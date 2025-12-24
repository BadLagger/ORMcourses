package sf.mifi.grechko.integration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final String TestPasswordRoleUser="testuser123";
    private final String AdminUsername="admin";
    private final String AdminPassword="admin123";

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
    @DisplayName("1. GET /api/users - получение всех пользователей без авторизации (ошибка 401)")
    void getAllUsers_WithoutAuthorization_ShouldReturnUnauthorized() {
        // Пытаемся получить доступ список пользователей без логина и пароля
        ResponseEntity<String> response = executeGet("/api/users", String.class,
                "", "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    @DisplayName("1. POST /api/users - создание нового пользователя USER (админ)")
    void postCreateUser_AdminAccess_ShouldReturnOk() throws JsonProcessingException {
        // Запрос
        Map<String, Object> userRequest = Map.of(
                "username", TestUsernameRoleUser,
                "password", TestPasswordRoleUser,
                "role", "USER"
        );
        String requestBody = objectMapper.writeValueAsString(userRequest);

        ResponseEntity<String> response = executePost("/api/users", requestBody,
                String.class, AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


}
