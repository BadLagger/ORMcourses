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

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssignmentControllerIntegrationTest extends BaseTest{
    private static final String baseEndpoint = "/api/assignments";
    private static Integer testId;

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        // Костыли, нужно разобраться почему не работает удаление в предыдущем модуле
        BaseTest.UserUsername = "assignUser";
        BaseTest.UserPassword = "assignUser123";

        BaseTest.TeacherUsername= "assignTeacher";
        BaseTest.TeacherPassword = "assignTeacher123";

        BaseTest.TestCategoryName = "Assignments";

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

        if (!createTestLessonViaApi()) {
            throw new IllegalStateException("Failed to set up test module. API may be unavailable.");
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. GET /api/assignments - получение всех заданий (доступ для всех авторизованных)")
    void getAllAssignments_ForAll_ShouldReturnOk() {
        // Админ получает список всех связей
        ResponseEntity<String> response = executeGet(baseEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает список всех связей
        response = executeGet(baseEndpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает список всех связей
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
    @DisplayName("2. POST /api/assignments - создание нового задания (ADMIN и TEACHER)")
    void createAssignment_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {

        Map<String, Object> requestWithRightId = Map.of(
                "title", "Test Assignment",
                "description", "Test Description",
                "dueDate", LocalDateTime.now().plusDays(10),
                "maxScore", 100,
                "lessonId", TestLessonId
        );

        Map<String, Object> requestSecondRightId = Map.of(
                "title", "Test Second Assignment",
                "description", "Test Second Description",
                "dueDate", LocalDateTime.now().plusDays(10),
                "maxScore", 100,
                "lessonId", TestLessonId
        );

        // Пытаемся добавить новое задание без авторизации
        ResponseEntity<String> response = executePost(baseEndpoint, requestWithRightId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Пытаемся добавить новое задание как простой пользователь
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся добавить новое задание как учитель
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пытаемся добавить новое задание как админ, но с неправильным ID
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Пытаемся добавить новое задание как админ
        response = executePost(baseEndpoint, requestSecondRightId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /api/assignments/{id} - получение задания по ID (для всех авторизированных)")
    void getAssignmentById_ForAll_ShouldReturnOk()  {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Админ получает задание по ID
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает задание по ID
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает задание по ID
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение задания по ID без авторизации
        response = executeGet(endpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Получение несуществующего задания по ID под ADMIN
        response = executeGet(wrongEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(4)
    @DisplayName("4. PUT /api/assignment/{id} - обновить задание (ADMIN и TEACHER)")
    void updateAssignment_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {
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

        // Попытка изменить задание без авторизации
        ResponseEntity<String> response = executePut(endpoint, requestOne, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка изменить задание от пользователя
        response = executePut(endpoint, requestOne, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить задание от учителя
        response = executePut(endpoint, requestOne, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleOne);

        // Попытка изменить задание от админа на несуществующем модуле
        response = executePut(wrongEndpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка изменить задание от админа
        response = executePut(endpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleTwo);
    }

    @Test
    @Order(5)
    @DisplayName("5. DELETE /api/assignment/{id} - удалить задание по ID (ADMIN и TEACHER)")
    void deleteAssignment_AdminAccess_ShouldReturnOk() {
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
