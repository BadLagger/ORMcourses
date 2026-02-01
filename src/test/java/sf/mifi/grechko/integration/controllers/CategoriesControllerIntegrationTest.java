package sf.mifi.grechko.integration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import sf.mifi.grechko.BaseTest;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoriesControllerIntegrationTest extends BaseTest {

    private static final String BASE_HOST_URL = "http://localhost:";

    private static final String AdminUsername="admin";
    private static final String AdminPassword="admin123";
    private static final String TeacherUsername="teacher";
    private static final String TeacherPassword="teacher123";
    private static final String UserUsername="user";
    private static final String UserPassword="user123";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static Integer testId;


    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        if (!createTestUsersViaApi(restTemplate, port)) {
            throw new IllegalStateException("Failed to set up test users. API may be unavailable.");
        }
    }

    private static boolean createTestUsersViaApi(TestRestTemplate restTemplate, int port) {
        Map<String, Object> teacherRequest = Map.of(
                "login", TeacherUsername,
                "password", TeacherPassword,
                "role", "TEACHER"
        );

        Map<String, Object> userRequest = Map.of(
                "login", UserUsername,
                "password", UserPassword,
                "role", "USER"
        );

        try {
            ResponseEntity<String> response = executePost("/api/users", teacherRequest,
                    String.class, AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            response = executePost("/api/users", userRequest,
                    String.class, AdminUsername, AdminPassword);

            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }

        } catch (Exception exp) {
            return false;
        }
        return true;
    }

    @Test
    @Order(23)
    @DisplayName("1. GET /api/categories - получение всех категорий (доступ для всех)")
    void getAllCategory_ForAll_ShouldReturnOk() {

        // Админ получает список всех категорий
        ResponseEntity<String> response = executeGet("/api/categories", String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает список всех категорий
        response = executeGet("/api/categories", String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает список всех категорий
        response = executeGet("/api/categories", String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение списка без авторизации
        response = executeGet("/api/categories", String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(24)
    @DisplayName("2. POST /api/categories - создание новой категории (доступ для ADMIN)")
    void createCategory_AdminAccess_ShouldReturnOk() throws JsonProcessingException {

        Map<String, Object> request = Map.of(
                "name", "programming languages"
        );

        // Учитель пытается создать категорию
        ResponseEntity<String> response = executePost("/api/categories", request, String.class,
                TeacherUsername, TeacherPassword);

        // Должна вернуться ошибка доступа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пользователь пытается создать категорию
        response = executePost("/api/categories", request, String.class,
                UserUsername, UserPassword);

        // Должна вернуться ошибка доступа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся создать категорию без авторизации
        response = executePost("/api/categories", request, String.class,
                null, null);

        // Должна вернуться ошибка авторизации
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Админ пытается создать категорию
        response = executePost("/api/categories", request, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        // Запомнить ID для следующего теста
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(25)
    @DisplayName("3. GET /api/categories/{id} - получение категории ID (доступ для всех)")
    void getCategoryById_ForAll_ShouldReturnOk() {

        // Запрос без авторизации получает категорию по ID
        ResponseEntity<String> response = executeGet("/api/categories/"+testId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Админ получает категорию по ID
        response = executeGet("/api/categories/"+testId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает категорию по ID
        response = executeGet("/api/categories/"+testId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает категорию по ID
        response = executeGet("/api/categories/"+testId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(26)
    @DisplayName("4. PUT /api/categories/{id} - изменить название категории (доступ для ADMIN)")
    void changeCategoryById_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        String newCategoryName = "math";

        Map<String, Object> request = Map.of(
                "name", newCategoryName
        );

        // Запрос без авторизации изменить категорию по ID
        ResponseEntity<String> response = executePut("/api/categories/"+testId, request, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Учитель изменяет категорию по ID
        response = executePut("/api/categories/"+testId, request, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пользователь изменяет категорию по ID
        response = executePut("/api/categories/"+testId, request, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Админ изменяет категорию по ID
        response = executePut("/api/categories/"+testId,  request, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("name").toString()).isEqualTo(newCategoryName);
    }

    @Test
    @Order(27)
    @DisplayName("5. DELETE /api/categories/{id} - удалить категорию (доступ для ADMIN)")
    void deleteCategoryById_AdminAccess_ShouldReturnOk() throws JsonProcessingException {

        // Запрос без авторизации удалить категорию по ID
        ResponseEntity<String> response = executeDelete("/api/categories/"+testId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Учитель удаляет категорию по ID
        response = executeDelete("/api/categories/"+testId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пользователь удаляет категорию по ID
        response = executeDelete("/api/categories/"+testId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Админ удаляет категорию по ID
        response = executeDelete("/api/categories/"+testId,  String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
