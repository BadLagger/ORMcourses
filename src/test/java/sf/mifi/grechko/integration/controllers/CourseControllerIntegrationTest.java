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
public class CourseControllerIntegrationTest extends BaseTest {

    private static Integer testId;
    private static final String baseEndpoint = "/api/courses";

    @BeforeAll
    static void setupAll(@Autowired TestRestTemplate restTemplate, @LocalServerPort int port) {
        BaseTest.restTemplate = restTemplate;
        BaseTest.baseUrl = BASE_HOST_URL + port;

        if (!createTestUsersViaApi(restTemplate, port)) {
            throw new IllegalStateException("Failed to set up test users. API may be unavailable.");
        }

        if (!createTestCategoryViaApi(restTemplate, port)) {
            throw new IllegalStateException("Failed to set up test category. API may be unavailable.");
        }

        if (!createAdditionalUsersViaApi()) {
            throw new IllegalStateException("Failed to set up additional users. API may be unavailable.");
        }
    }

    @AfterAll
    static void cleanUp() {
        /*if (!deleteTestUsersViaApi()) {
            throw new IllegalStateException("Failed to clean up test users. API may be unavailable.");
        }*/

        if (!deleteAdditionUsersViaApi()) {
            throw new IllegalStateException("Failed to clean up additional users. API may be unavailable.");
        }

        /*if (!deleteTestCategotiesViaApi()) {
            throw new IllegalStateException("Failed to clean up test categories. API may be unavailable.");
        }*/
    }

    @Test
    @Order(28)
    @DisplayName("1. GET /api/courses - получение всех курсов (доступ для всех)")
    void getAllCourses_ForAll_ShouldReturnOk() {

        // Админ получает список всех категорий
        ResponseEntity<String> response = executeGet(baseEndpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает список всех категорий
        response = executeGet(baseEndpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает список всех категорий
        response = executeGet(baseEndpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение списка без авторизации
        response = executeGet(baseEndpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(29)
    @DisplayName("2. POST /api/courses - создание нового курса (доступ для ADMIN и TEACHER)")
    void createCourse_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {
        Map<String, Object> requestWithoutTeacherId = Map.of(
                "title", "REST API with Java",
                "description", "test course",
                "categoryId", TestCategoryId,
                "duration", "5 weeks"
        );

        Map<String, Object> requestWithTeacherId = Map.of(
                "title", "REST API with Java",
                "description", "test course",
                "categoryId", TestCategoryId,
                "teacherId", TestTeacherId,
                "duration", "5 weeks"
        );

        Map<String, Object> requestNewWithTeacherId = Map.of(
                "title", "Algorithms: Java",
                "description", "test course",
                "categoryId", TestCategoryId,
                "teacherId", TestTeacherId,
                "duration", "5 weeks"
        );

        // Пытаемся добавить новый курс без авторизации
        ResponseEntity<String> response = executePost(baseEndpoint, requestWithoutTeacherId, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Пытаемся добавить новый курс как простой пользователь
        response = executePost(baseEndpoint, requestWithoutTeacherId, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пытаемся создать новый курс под админом, но без teacherId
        response = executePost(baseEndpoint, requestWithoutTeacherId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Пытаемся создать новый курс под админом c teacherId
        response = executePost(baseEndpoint, requestWithTeacherId, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пытаемся создать новый курс под учителем, но с дублирующим названием
        response = executePost(baseEndpoint, requestWithoutTeacherId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Пытаемся создать новый курс под учителем
        response = executePost(baseEndpoint, requestNewWithTeacherId, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());
    }


    @Test
    @Order(30)
    @DisplayName("3. GET /api/courses/{id} - получение курса по ID (доступ для всех)")
    void getCourseById_ForAll_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/" + testId;
        String wrongEndpoint = baseEndpoint + "/" + (testId + 1);

        // Админ получает курс по ID
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает курс по ID
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает курс по ID
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение курса по ID без авторизации
        response = executeGet(endpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение несуществующего курса по ID без авторизации
        response = executeGet(wrongEndpoint, String.class,
                null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(31)
    @DisplayName("4. GET /api/courses/teacher/{teacherId} - получение курса по ID учителя (доступ для всех)")
    void getCourseByTeacherId_ForAll_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/teacher/" + TestTeacherId;
        String wrongEndpoint = baseEndpoint + "/teacher/" + (TestTeacher2Id + 1);
        String wrong2Endpoint = baseEndpoint + "/teacher/" + (TestTeacher2Id);

        // Админ получает курс по ID учителя
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает курс по ID учителя
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает курс по ID учителя
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение курсов по ID учителя без авторизации
        response = executeGet(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение несуществующих курсов учителя по ID без авторизации
        response = executeGet(wrongEndpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Получение списка курсов по ID учителя без курсов
        response = executeGet(wrong2Endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(32)
    @DisplayName("4. GET /api/courses/category/{categoryId} - получение курса по ID категории (доступ для всех)")
    void getCourseByCategoryId_ForAll_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/category/" + TestCategoryId;
        String wrongEndpoint = baseEndpoint + "/category/" + (TestCategoryId + 1);

        // Админ получает курс по ID категории
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель получает курс по ID категории
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Пользователь получает курс по ID категории
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение курсов по ID категории без авторизации
        response = executeGet(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Получение несуществующих курсов по ID категории без авторизации
        response = executeGet(wrongEndpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(33)
    @DisplayName("5. GET /api/courses/my - получение своих курсов (доступ для TEACHER)")
    void getCourseMy_TeacherAccess_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/my";
       // String wrongEndpoint = baseEndpoint + "/category/" + (TestCategoryId + 1);

        // Админ пытается получить свои курсы
        ResponseEntity<String> response = executeGet(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Пользователь пытается получить свои курсы
        response = executeGet(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка получить свои курсы без авторизации
        response = executeGet(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Учитель получает свои курсы
        response = executeGet(endpoint, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Учитель без курсов пытается получить свои курсы
        response = executeGet(endpoint, String.class,
                Teacher2Username, Teacher2Password);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(34)
    @DisplayName("6. PUT /api/courses/{id} - обновить информацию о курсе (для TEACHER только свои курсы, ADMIN все)")
    void putCourseById_AdminTeacherAccess_ShouldReturnOk() {
        String endpoint = baseEndpoint + "/" + testId;
        String newTitle = "ORM by Java";

        Map<String, Object> requestWithoutTeacherId = Map.of(
                "title", newTitle
        );

        // Попытка изменить инфо о курсе без авторизации
        ResponseEntity<String> response = executePut(endpoint, requestWithoutTeacherId, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка изменить инфо о курсе от имени другого учителя
        response = executePut(endpoint, requestWithoutTeacherId, String.class,
                Teacher2Username, Teacher2Password);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка изменить инфо о курсе от имени обычного пользователя
        response = executePut(endpoint, requestWithoutTeacherId, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка изменить инфо о курсе от имени учителя-владельца
        response = executePut(endpoint, requestWithoutTeacherId, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Попытка изменить инфо о курсе от имени администратора
        response = executePut(endpoint, requestWithoutTeacherId, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(35)
    @DisplayName("7. DELETE /api/courses/{id} - удалить курс по ID (для TEACHER только свои курсы, ADMIN все)")
    void deleteCourseById_AdminTeacherAccess_ShouldReturnOk() throws JsonProcessingException {
        String endpoint = baseEndpoint + "/" + testId;

        Map<String, Object> requestTeacherId = Map.of(
                "title", "REST API with Java",
                "description", "test course",
                "categoryId", TestCategoryId,
                "duration", "5 weeks"
        );

        // Попытка удалить курс без авторизации
        ResponseEntity<String> response = executeDelete(endpoint, String.class,
                null, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Попытка удалить курс от имени другого учителя
        response = executeDelete(endpoint, String.class,
                Teacher2Username, Teacher2Password);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Попытка удалить курс от обычного пользователя
        response = executeDelete(endpoint, String.class,
                UserUsername, UserPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Попытка удалить курс от учителя-владельца
        response = executeDelete(endpoint, String.class,
                TeacherUsername, TeacherPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Создаём новые курсы для второго учителя
        response = executePost(baseEndpoint, requestTeacherId, String.class,
                Teacher2Username, Teacher2Password);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        testId = Integer.valueOf(responseBody.get("id").toString());
        endpoint = baseEndpoint + "/" + testId;

        // Пытаемся удалить новые курсы под админом
        response = executeDelete(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Пытаемся удалить несуществующие курсы
        response = executeDelete(endpoint, String.class,
                AdminUsername, AdminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
