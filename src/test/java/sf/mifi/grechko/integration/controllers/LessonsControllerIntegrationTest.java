package sf.mifi.grechko.integration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import sf.mifi.grechko.BaseTest;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LessonsControllerIntegrationTest extends BaseTest{
    private static final String baseEndpoint = "/api/lessons";
    private static Integer testId;

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        // Костыли, нужно разобраться почему не работает удаление в предыдущем модуле
        BaseTest.UserUsername = "lesUser";
        BaseTest.UserPassword = "lesUser123";

        BaseTest.TeacherUsername= "lesTeacher";
        BaseTest.TeacherPassword = "lesTeacher123";

        BaseTest.TestCategoryName = "Ruby";

        if (!createTestUsersViaApi()) {
            throw new IllegalStateException("Failed to set up test users. API may be unavailable.");
        }

        if (!createTestCategoryViaApi()) {
            throw new IllegalStateException("Failed to set up test category. API may be unavailable.");
        }

        if (!createTestCourseViaApi()) {
            throw new IllegalStateException("Failed to set up test course. API may be unavailable.");
        }

        if (!createTestModuleViaApi()) {
            throw new IllegalStateException("Failed to set up test module. API may be unavailable.");
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. GET /api/lessons - получение всех уроков (доступ только для авторизированных)")
    void getAllLessons_ForAll_ShouldReturnOk() {
        // Админ получает список всех уроков
        ResponseEntity<String> response = executeGet(baseEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает список всех уроков
        response = executeGet(baseEndpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает список всех уроков
        response = executeGet(baseEndpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение списка без авторизации
        response = executeGet(baseEndpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    @DisplayName("2. POST /api/lessons - создание нового урока (ADMIN и TEACHER)")
    void createLesson_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {

        Map<String, Object> requestWithRightId = Map.of(
                "title", "Test Lesson",
                "content", "Test Content",
                "videoUrl", "http://my.lesson",
                "moduleId", TestModuleId
        );

        Map<String, Object> requestWithSecondId = Map.of(
                "title", "Test Second Lesson",
                "content", "Test Second Content",
                "videoUrl", "http://my.lesson2",
                "moduleId", TestModuleId
        );

        // Пытаемся добавить новый урок без авторизации
        ResponseEntity<String> response = executePost(baseEndpoint, requestWithRightId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Пытаемся добавить новый урок как простой пользователь
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся добавить новый урок как учитель
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пытаемся добавить новый урок как админ, но с неправильным ID
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Пытаемся добавить новый урок как админ
        response = executePost(baseEndpoint, requestWithSecondId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /api/lessons/{id} - получение урока по ID (для всех авторизированных)")
    void getEnrollmentById_ForAll_ShouldReturnOk()  {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Админ получает урок по ID
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает урок по ID
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает урок по ID
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение связи по ID без авторизации
        response = executeGet(endpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Получение несуществующей связи по ID под ADMIN
        response = executeGet(wrongEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(4)
    @DisplayName("4. PUT /api/lessons/{id} - обновить урок (ADMIN и TEACHER)")
    void updateLesson_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);
        String newTitleOne = "New Title 1";
        String newTitleTwo = "New Title 2";

        Map<String, Object> requestOne = Map.of(
                "title", newTitleOne
        );

        Map<String, Object> requestTwo = Map.of(
                "title", newTitleTwo
        );

        // Попытка изменить урок без авторизации
        ResponseEntity<String> response = executePut(endpoint, requestOne, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка изменить урок от пользователя
        response = executePut(endpoint, requestOne, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить урок от учителя
        response = executePut(endpoint, requestOne, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleOne);

        // Попытка изменить урок от админа на несуществующем модуле
        response = executePut(wrongEndpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка изменить урок от админа
        response = executePut(endpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleTwo);
    }

    @Test
    @Order(5)
    @DisplayName("5. DELETE /api/lessons/{id} - удалить урок по ID (только для ADMIN)")
    void deleteEnrollment_AdminTeacherAccess_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/" + testId;
        String secondEndpoint = baseEndpoint + "/" + (testId - 1);
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Попытка удалить урок без авторизации
        ResponseEntity<String> response = executeDelete(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка удалить урок под пользователем
        response = executeDelete(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка удалить урок под учителем
        response = executeDelete(secondEndpoint, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Попытка удалить несуществующую урок
        response = executeDelete(wrongEndpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка удалить урок под админом
        response = executeDelete(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
