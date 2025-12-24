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
public class ProfileControllerIntegrationTest extends BaseTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String AdminUsername="admin";
    private final String AdminPassword="admin123";
    private final String TestBioInfo="Test Bio Info";
    private final String TestAvatarUrl="http://test.avatar.url";
    private final String TestEmail="test@avatar.url";
    private final String TestEmail2="test2@avatar.url";

    private final String TestUsernameRoleTeacher="testteacher";
    private final String TestPasswordRoleTeacher="testteacher123";

    private static Integer testId;

    @BeforeEach
    void setUp() {
        this.restTemplate = template;
        this.baseUrl = "http://localhost:" + port;
    }

    @Test
    @Order(11)
    @DisplayName("1. PUT /api/profiles/me - прописываем собственный профиль (админ)")
    void putProfile_AdminAccess_ShouldReturnOk() {

        // Запрос
        Map<String, Object> userRequest = Map.of(
                "bio", TestBioInfo,
                "avatarUrl", TestAvatarUrl,
                "email", TestEmail
        );

        // Админ получает список пользователей
        ResponseEntity<String> response = executePut("/api/profiles/me", userRequest, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(12)
    @DisplayName("2. GET /api/profiles/me - получаем собственный профиль (админ)")
    void getProfile_AdminAccess_ShouldReturnOk() throws JsonProcessingException {

        ResponseEntity<String> response = executeGet("/api/profiles/me", String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("bio").toString()).isEqualTo(TestBioInfo);
        assertThat(responseBody.get("avatarUrl").toString()).isEqualTo(TestAvatarUrl);
        assertThat(responseBody.get("email").toString()).isEqualTo(TestEmail);
    }

    @Test
    @Order(13)
    @DisplayName("3. POST /api/users - создание нового пользователя TEACHER (админ)")
    void postCreateUser_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "login", TestUsernameRoleTeacher,
                "password", TestPasswordRoleTeacher,
                "role", "TEACHER"
        );

        ResponseEntity<String> response = executePost("/api/users", userRequest,
                String.class, AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("login").toString()).isEqualTo(TestUsernameRoleTeacher);
        assertThat(responseBody.get("role").toString()).isEqualTo("TEACHER");

        // Запомнить ID для следующего теста
        testId = Integer.valueOf(responseBody.get("id").toString());
    }

    @Test
    @Order(14)
    @DisplayName("4. PUT /api/profiles/me - прописываем собственный профиль с уже существующим email (пользователь TEACHER, ошибка 500)")
    void putProfile_TeacherAccess_ShouldReturnInternalServerError() {

        // Запрос
        Map<String, Object> userRequest = Map.of(
                "bio", TestBioInfo,
                "avatarUrl", TestAvatarUrl,
                "email", TestEmail
        );

        // Админ получает список пользователей
        ResponseEntity<String> response = executePut("/api/profiles/me", userRequest, String.class,
                TestUsernameRoleTeacher, TestPasswordRoleTeacher);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @Order(15)
    @DisplayName("5. PUT /api/profiles/me - прописываем собственный профиль (пользователь TEACHER)")
    void putProfile_TeacherAccess_ShouldReturnOk() {

        // Запрос
        Map<String, Object> userRequest = Map.of(
                "bio", TestBioInfo,
                "avatarUrl", TestAvatarUrl,
                "email", TestEmail2
        );

        // Админ получает список пользователей
        ResponseEntity<String> response = executePut("/api/profiles/me", userRequest, String.class,
                TestUsernameRoleTeacher, TestPasswordRoleTeacher);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(16)
    @DisplayName("6. GET /api/profiles/me - получаем собственный профиль (пользователь TEACHER)")
    void getProfile_TeacherAccess_ShouldReturnOk() throws JsonProcessingException {

        ResponseEntity<String> response = executeGet("/api/profiles/me", String.class,
                TestUsernameRoleTeacher, TestPasswordRoleTeacher);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("bio").toString()).isEqualTo(TestBioInfo);
        assertThat(responseBody.get("avatarUrl").toString()).isEqualTo(TestAvatarUrl);
        assertThat(responseBody.get("email").toString()).isEqualTo(TestEmail2);
    }

    @Test
    @Order(17)
    @DisplayName("7. GET /api/profiles/user/{id} - получаем профиль по id (пользователь TEACHER, ошибка 403)")
    void getProfileById_TeacherAccess_ShouldReturnForbidden() throws JsonProcessingException {

        System.out.printf("Test ID: %d%n", testId);
        String url = String.format("/api/profiles/user/%d", testId);
        ResponseEntity<String> response = executeGet(url, String.class,
                TestUsernameRoleTeacher, TestPasswordRoleTeacher);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(18)
    @DisplayName("8. GET /api/profiles/user/{id} - получаем профиль по id (пользователь админ)")
    void getProfileById_AdminAccess_ShouldReturnOk() throws JsonProcessingException {

        String url = String.format("/api/profiles/user/%d", testId);
        ResponseEntity<String> response = executeGet(url, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("bio").toString()).isEqualTo(TestBioInfo);
        assertThat(responseBody.get("avatarUrl").toString()).isEqualTo(TestAvatarUrl);
        assertThat(responseBody.get("email").toString()).isEqualTo(TestEmail2);
    }

    @Test
    @Order(19)
    @DisplayName("9. PUT /api/profiles/user/{id} - прописываем профиль по id (пользователь TEACHER, ошибка 403)")
    void putProfileBy_TeacherAccess_ShouldReturnForbidden() {

        String url = String.format("/api/profiles/user/%d", testId);
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "bio", "Some New Info",
                "avatarUrl", "Some new avatar",
                "email", "Some new email"
        );

        // Админ получает список пользователей
        ResponseEntity<String> response = executePut(url, userRequest, String.class,
                TestUsernameRoleTeacher, TestPasswordRoleTeacher);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(20)
    @DisplayName("10. PUT /api/profiles/user/{id} - прописываем профиль по id (пользователь админ)")
    void putProfileBy_TeacherAccess_ShouldReturnOk() throws JsonProcessingException {

        final String newInfo = "Some New Info";
        final String newAvatar = "http://some.new.avatar";
        final String newEmail = "http://new@email.com";
        String url = String.format("/api/profiles/user/%d", testId);
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "bio", newInfo,
                "avatarUrl", newAvatar,
                "email", newEmail
        );

        // Админ получает список пользователей
        ResponseEntity<String> response = executePut(url, userRequest, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        assertThat(responseBody.get("bio").toString()).isEqualTo(newInfo);
        assertThat(responseBody.get("avatarUrl").toString()).isEqualTo(newAvatar);
        assertThat(responseBody.get("email").toString()).isEqualTo(newEmail);
    }

    @Test
    @Order(21)
    @DisplayName("11. DELETE /api/users/{id} - удаление пользователя пользователя TEACHER (админ)")
    void deleteUser_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        String url = String.format("/api/users/%d", testId);
        ResponseEntity<String> response = executeDelete(url,String.class, AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(22)
    @DisplayName("12. GET /api/profiles/user/{id} - получаем профиль по id после удаления пользователя (пользователь админ, ошибка 404)")
    void getProfileById_AdminAccess_ShouldReturnNotFound() throws JsonProcessingException {

        String url = String.format("/api/profiles/user/%d", testId);
        ResponseEntity<String> response = executeGet(url, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}

