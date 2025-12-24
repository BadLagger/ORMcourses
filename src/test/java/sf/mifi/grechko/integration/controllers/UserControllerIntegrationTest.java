package sf.mifi.grechko.integration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class UserControllerIntegrationTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    private final String TestUsernameRoleUser="testuser";
    private static String TestPasswordRoleUser="testuser123";
    private final String TestUsernameRoleTeacher="testteacher";
    private final String TestPasswordRoleTeacher="testteacher123";
    private final String AdminUsername="admin";
    private final String AdminPassword="admin123";

    private static Integer testId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.restTemplate = template;
        this.baseUrl = "http://localhost:" + port;
    }

    @Test
    @Order(1)
    @DisplayName("1. GET /api/users - получение всех пользователей (админ)")
    void getAllUsers_AdminAccess_ShouldReturnOk() {
        // Админ получает список пользователей
        ResponseEntity<String> response = executeGet("/api/users", String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /api/users - получение всех пользователей без авторизации (ошибка 401)")
    void getAllUsers_WithoutAuthorization_ShouldReturnUnauthorized() {
        // Пытаемся получить доступ список пользователей без логина и пароля
        ResponseEntity<String> response = executeGet("/api/users", String.class,
                "", "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    @DisplayName("3. POST /api/users - создание нового пользователя USER (админ)")
    void postCreateUser_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "login", TestUsernameRoleUser,
                "password", TestPasswordRoleUser,
                "role", "USER"
        );

        ResponseEntity<String> response = executePost("/api/users", userRequest,
                String.class, AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("login").toString()).isEqualTo(TestUsernameRoleUser);
        assertThat(responseBody.get("role").toString()).isEqualTo("USER");

        // Запомнить ID для следующего теста
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(4)
    @DisplayName("4. POST /api/users - создание нового пользователя TEACHER (от имени пользователя, ошибка 403)")
    void postCreateUser_UserAccess_ShouldReturnForbidden() throws JsonProcessingException {
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "login", TestUsernameRoleTeacher,
                "password", TestPasswordRoleTeacher,
                "role", "TEACHER"
        );

        ResponseEntity<String> response = executePost("/api/users", userRequest,
                String.class, TestUsernameRoleUser, TestPasswordRoleUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(5)
    @DisplayName("5. PUT /api/users/{id}/role - изменить роль пользователя на TEACHER (админ)")
    void putChangeUser_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        // Запрос
        String url = String.format("/api/users/%d/role?role=TEACHER", testId);

        ResponseEntity<String> response = executePut(url, String.class, AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("role").toString()).isEqualTo("TEACHER");
    }

    @Test
    @Order(6)
    @DisplayName("6. PUT /api/users/{id}/role - изменить роль пользователя на USER (от имени пользователя, ошибка 403)")
    void putChangeUser_TeacherAccess_ShouldReturnForbidden() throws JsonProcessingException {
        // Запрос
        String url = String.format("/api/users/%d/role?role=USER", testId);
        ResponseEntity<String> response = executePut(url, String.class, TestUsernameRoleUser, TestPasswordRoleUser);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(7)
    @DisplayName("7. POST /api/users/{id}/change-password - изменение пароля пользователя TEACHER (админ)")
    void postChangePassword_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        String newPassword = "new_pass";
        String url = String.format("/api/users/%d/change-password", testId);
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "currentPassword", TestPasswordRoleUser,
                "newPassword", newPassword
        );

        ResponseEntity<String> response = executePost(url, userRequest,
                String.class, AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TestPasswordRoleUser = newPassword;
    }

    @Test
    @Order(8)
    @DisplayName("8. POST /api/users/{id}/change-password - изменение пароля пользователя TEACHER (от имени пользователя, ошибка 403)")
    void postChangePassword_TeacherAccess_ShouldReturnForbidden() throws JsonProcessingException {
        String url = String.format("/api/users/%d/change-password", testId);
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "currentPassword", TestPasswordRoleUser,
                "newPassword", "testpass"
        );

        ResponseEntity<String> response = executePost(url, userRequest,
                String.class, TestUsernameRoleUser, TestPasswordRoleUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    @Order(9)
    @DisplayName("9. DELETE /api/users/{id} - удаление пользователя пользователя TEACHER (от имени пользователя, ошибка 403)")
    void deleteUser_TeacherAccess_ShouldReturnForbidden() throws JsonProcessingException {
        String url = String.format("/api/users/%d", testId);
        ResponseEntity<String> response = executeDelete(url,String.class, TestUsernameRoleUser, TestPasswordRoleUser);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(10)
    @DisplayName("10. DELETE /api/users/{id} - удаление пользователя пользователя TEACHER (админ)")
    void deleteUser_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        String url = String.format("/api/users/%d", testId);
        ResponseEntity<String> response = executeDelete(url,String.class, AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
