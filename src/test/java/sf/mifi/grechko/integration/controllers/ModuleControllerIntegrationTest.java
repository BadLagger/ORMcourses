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
public class ModuleControllerIntegrationTest extends BaseTest {
    private static final String baseEndpoint = "/api/modules";
    private static Integer testId;

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        // Костыли, нужно разобраться почему не работает удаление в предыдущем модуле
        BaseTest.UserUsername = "modUser";
        BaseTest.UserPassword = "modUser123";

        BaseTest.TeacherUsername= "modTeacher";
        BaseTest.TeacherPassword = "modTeacher123";

        BaseTest.TestCategoryName = "Python";

        if (!createTestUsersViaApi(restTemplate, port)) {
            throw new IllegalStateException("Failed to set up test users. API may be unavailable.");
        }

        if (!createTestCategoryViaApi(restTemplate, port)) {
            throw new IllegalStateException("Failed to set up test category. API may be unavailable.");
        }

        if (!createTestCourseViaApi()) {
            throw new IllegalStateException("Failed to set up test course. API may be unavailable.");
        }
    }

    @Test
    @Order(42)
    @DisplayName("1. GET /api/modules - получение всех модулей (доступ для всех)")
    void getAllModules_ForAll_ShouldReturnOk() {
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(43)
    @DisplayName("2. POST /api/modules - создание нового модуля (ADMIN и TEACHER)")
    void createModules_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {

        Map<String, Object> requestWithRightId = Map.of(
                "title", "Introduction",
                "description", "Test description",
                "courseId", TestCourseId
        );

        Map<String, Object> requestWithSecondId = Map.of(
                "title", "Second Module",
                "description", "Test description for second",
                "courseId", TestCourseId
        );

        // Пытаемся добавить новый модуль без авторизации
        ResponseEntity<String> response = executePost(baseEndpoint, requestWithRightId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Пытаемся добавить новый модуль как простой пользователь
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся добавить новый модуль как учитель
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пытаемся добавить новый модуль как админ, но с повторным ID
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Пытаемся добавить новый модуль как админ
        response = executePost(baseEndpoint, requestWithSecondId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(44)
    @DisplayName("3. GET /api/modules/{id} - получение модуля по ID (для всех)")
    void getModuleById_ForAll_ShouldReturnOk() throws JsonProcessingException {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Админ получает связь по ID
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает связь по ID
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает связь по ID
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение связи по ID без авторизации
        response = executeGet(endpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение несуществующей связи по ID без авторизации
        response = executeGet(wrongEndpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(45)
    @DisplayName("4. PUT /api/modules/{id} - обновить модуль (ADMIN и TEACHER)")
    void updateModule_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {
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

        // Попытка изменить модуль без авторизации
        ResponseEntity<String> response = executePut(endpoint, requestOne, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка изменить модуль от пользователя
        response = executePut(endpoint, requestOne, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить модуль от учителя
        response = executePut(endpoint, requestOne, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleOne);

        // Попытка изменить модуль от админа на несуществующем модуле
        response = executePut(wrongEndpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка изменить модуль от админа
        response = executePut(endpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleTwo);
    }

    @Test
    @Order(46)
    @DisplayName("5. DELETE /api/modules/{id} - удалить модуль по ID (только для ADMIN)")
    void deleteEnrollment_AdminAccess_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Попытка удалить связь без авторизации
        ResponseEntity<String> response = executeDelete(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка удалить связь под пользователем
        response = executeDelete(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка удалить связь под учителем
        response = executeDelete(endpoint, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка удалить несуществующую связь
        response = executeDelete(wrongEndpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка удалить связь под админом
        response = executeDelete(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


}
