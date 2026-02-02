package sf.mifi.grechko.integration.controllers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import sf.mifi.grechko.BaseTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubmissionControllerIntegrationTest extends BaseTest {
    private static final String baseEndpoint = "/api/submissions";
    private static Integer testId;

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        // Костыли, нужно разобраться почему не работает удаление в предыдущем модуле
        BaseTest.UserUsername = "subUser";
        BaseTest.UserPassword = "subUser123";

        BaseTest.TeacherUsername= "subTeacher";
        BaseTest.TeacherPassword = "subTeacher123";

        BaseTest.TestCategoryName = "Submission";

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
    @DisplayName("1. GET /api/submissions - получение всех ответов (доступ только для авторизированных)")
    void getAllSubmissions_ForAll_ShouldReturnOk() {
        // Админ получает список всех ответов
        ResponseEntity<String> response = executeGet(baseEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает список всех ответов
        response = executeGet(baseEndpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает список всех ответов
        response = executeGet(baseEndpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение списка без авторизации
        response = executeGet(baseEndpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
