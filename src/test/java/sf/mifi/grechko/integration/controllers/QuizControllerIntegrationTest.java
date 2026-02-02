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
public class QuizControllerIntegrationTest extends BaseTest {
    private static final String baseEndpoint = "/api/quizzes";
    private static Integer testId;

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        // Костыли, нужно разобраться почему не работает удаление в предыдущем модуле
        BaseTest.UserUsername = "qUser";
        BaseTest.UserPassword = "qUser123";

        BaseTest.TeacherUsername= "qTeacher";
        BaseTest.TeacherPassword = "qTeacher123";

        BaseTest.TestCategoryName = "Quiz";

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
    void getQuizzes_ForAll_ShouldReturnOk() {
        // Админ получает список всех тестов
        ResponseEntity<String> response = executeGet(baseEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает список всех тестов
        response = executeGet(baseEndpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает список всех тестов
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
    @DisplayName("2. POST /api/quizzes - создание нового урока (ADMIN и TEACHER)")
    void createQuizzes_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {

        Map<String, Object> requestWithRightId = Map.of(
                "title", "Test quiz",
                "description", "Test description",
                "timeLimitMinutes", 30,
                "passingScore", 60,
                "maxAttempts", 5,
                "moduleId", TestModuleId
        );

        // Пытаемся добавить новый тест без авторизации
        ResponseEntity<String> response = executePost(baseEndpoint, requestWithRightId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Пытаемся добавить новый тест как простой пользователь
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся добавить новый тест как учитель
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());

        // Пытаемся добавить новый тест как админ, но с неправильным ID
        response = executePost(baseEndpoint, requestWithRightId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /api/quizzes/{id} - получение урока по ID (для всех авторизированных)")
    void getEnrollmentById_ForAll_ShouldReturnOk()  {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Админ получает тест по ID
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает тест по ID
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает тест по ID
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение теста по ID без авторизации
        response = executeGet(endpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Получение несуществующего теста по ID под ADMIN
        response = executeGet(wrongEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(4)
    @DisplayName("4. PUT /api/quizzes/{id} - обновить урок (ADMIN и TEACHER)")
    void updateQuiz_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {
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

        // Попытка изменить тесты без авторизации
        ResponseEntity<String> response = executePut(endpoint, requestOne, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка изменить тесты от пользователя
        response = executePut(endpoint, requestOne, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить тесты от учителя
        response = executePut(endpoint, requestOne, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleOne);

        // Попытка изменить тесты от админа на несуществующем модуле
        response = executePut(wrongEndpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка изменить тесты от админа
        response = executePut(endpoint, requestTwo, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(responseBody.get("title").toString()).isEqualTo(newTitleTwo);
    }

    @Test
    @Order(5)
    @DisplayName("5. DELETE /api/quizzes/{id} - удалить урок по ID (ADMIN и TEACHER)")
    void deleteQuiz_AdminTeacherAccess_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Попытка удалить тесты без авторизации
        ResponseEntity<String> response = executeDelete(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка удалить тесты под пользователем
        response = executeDelete(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка удалить несуществующий тесты
        response = executeDelete(wrongEndpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка удалить тесты под админом
        response = executeDelete(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
