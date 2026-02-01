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
public class EnrollmentControllerIntegrationTest extends BaseTest {

    private static final String baseEndpoint = "/api/enrollments";
    private static Integer testId;

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        // Костыли, нужно разобраться почему не работает удаление в предыдущем модуле
        BaseTest.UserUsername = "enrUser";
        BaseTest.UserPassword = "enrUser123";

        BaseTest.TeacherUsername= "enrTeacher";
        BaseTest.TeacherPassword = "enrTeacher123";

        BaseTest.TestCategoryName = "C++";

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
    @Order(36)
    @DisplayName("1. GET /api/enrollments - получение всех связей пользователей и курсов (доступ для всех)")
    void getAllEnrollments_ForAll_ShouldReturnOk() {
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
    @Order(37)
    @DisplayName("2. POST /api/enrollments - создание новой связи пользователя и курса (только ADMIN)")
    void createEnrollment_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        Map<String, Object> requestWithWrongId = Map.of(
                "userId", TestTeacherId,
                "courseId", TestCourseId
        );

        Map<String, Object> requestWithRightId = Map.of(
                "userId", TestUserId,
                "courseId", TestCourseId
        );

        // Пытаемся добавить новый курс без авторизации
        ResponseEntity<String> response = executePost(baseEndpoint, requestWithRightId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Пытаемся добавить новый курс как простой пользователь
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся добавить новый курс как учитель
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся добавить новый курс как админ, но с неправильным ID
        response = executePost(baseEndpoint, requestWithWrongId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Пытаемся добавить новый курс как админ
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(38)
    @DisplayName("3. GET /api/enrollments/{id} - получение связи по ID (для всех)")
    void getEnrollmentById_ForAll_ShouldReturnOk() throws JsonProcessingException {
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
    @Order(39)
    @DisplayName("4. GET /api/enrollments/my - получение своих связей (для всех авторизированных)")
    void getMyEnrollment_ForAll_ShouldReturnOk() throws JsonProcessingException {
        String endpoint = baseEndpoint + "/my";

        // Админ получает связь по ID
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Пользователь получает связь по ID
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение связи по ID без авторизации
        response = executeGet(endpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(40)
    @DisplayName("4. PUT /api/enrollments/{id} - обновить статус связи (только для ADMIN)")
    void updateEnrollmentStatus_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);
        String newStatus = "COMPLETED";

        Map<String, Object> request = Map.of(
                "status", newStatus
        );

        // Попытка изменить статус без авторизации
        ResponseEntity<String> response = executePut(endpoint, request, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка изменить статус от пользователя
        response = executePut(endpoint, request, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить статус от учителя
        response = executePut(endpoint, request, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить статус от админа на несуществующей связи
        response = executePut(wrongEndpoint, request, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка изменить статус от админа
        response = executePut(endpoint, request, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("status").toString()).isEqualTo(newStatus);
    }

    @Test
    @Order(41)
    @DisplayName("5. DELETE /api/enrollments/{id} - удалить связь по ID (только для ADMIN)")
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
