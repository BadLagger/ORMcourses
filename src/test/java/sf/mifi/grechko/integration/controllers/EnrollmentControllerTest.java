package sf.mifi.grechko.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import sf.mifi.grechko.BaseTest;
import sf.mifi.grechko.dto.EnrollmentDto;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EnrollmentControllerTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private Integer testUserId;
    private Integer testCourseId;
    private Integer testEnrollmentId;

    // Тестовые учетные данные (предполагаем, что они есть в БД)
    private final String adminUsername = "admin";
    private final String adminPassword = "admin123";
    private final String teacherUsername = "teacher";
    private final String teacherPassword = "teacher123";
    private final String studentUsername = "student";
    private final String studentPassword = "student123";

    @BeforeEach
    void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api";
        this.restTemplate = new TestRestTemplate();

        // Создаем тестового пользователя (студента) и курс, если их нет
        // Предполагаем, что в БД уже есть минимальные тестовые данные
        initializeTestData();
    }

    private void initializeTestData() {
        // Создаем тестового пользователя (студента)
        Map<String, Object> studentRequest = Map.of(
                "login", "test_student_" + System.currentTimeMillis(),
                "password", "testpass123",
                "role", "USER",
                "name", "Test Student"
        );

        ResponseEntity<Map> studentResponse = executePost(
                "/api/users",
                studentRequest,
                Map.class,
                adminUsername,
                adminPassword
        );

        if (studentResponse.getStatusCode() == HttpStatus.CREATED) {
            this.testUserId = (Integer) studentResponse.getBody().get("id");
        } else {
            // Если пользователь уже существует, ищем его по логину
            // В реальном тесте нужно адаптировать под вашу логику
            this.testUserId = 2; // предполагаем, что студент с id=2
        }

        // Создаем тестовый курс
        Map<String, Object> courseRequest = Map.of(
                "title", "Test Course for Enrollment",
                "description", "Course description",
                "categoryId", 1, // предполагаем, что есть категория с id=1
                "teacherId", 1,  // предполагаем, что учитель с id=1
                "duration", "10 weeks",
                "startDate", "2024-12-01"
        );

        ResponseEntity<Map> courseResponse = executePost(
                "/api/courses",
                courseRequest,
                Map.class,
                adminUsername,
                adminPassword
        );

        if (courseResponse.getStatusCode() == HttpStatus.CREATED) {
            this.testCourseId = (Integer) courseResponse.getBody().get("id");
        } else {
            // Если курс уже существует
            this.testCourseId = 1; // предполагаем, что курс с id=1
        }
    }

    @Test
    void testCreateAndGetEnrollment() throws Exception {
        // 1. Создаем enrollment
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setEnrollDate(LocalDate.now());
        createRequest.setStatus("ACTIVE");

        ResponseEntity<EnrollmentDto> createResponse = executePost(
                "/enrollments",
                createRequest,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());

        this.testEnrollmentId = createResponse.getBody().getId();

        // 2. Получаем созданный enrollment
        ResponseEntity<EnrollmentDto> getResponse = executeGet(
                "/enrollments/" + testEnrollmentId,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(testEnrollmentId, getResponse.getBody().getId());
        assertEquals(testUserId, getResponse.getBody().getUserId());
        assertEquals(testCourseId, getResponse.getBody().getCourseId());
        assertEquals("ACTIVE", getResponse.getBody().getStatus());
    }

    @Test
    void testGetEnrollmentsByUser() {
        // 1. Сначала создаем enrollment
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setStatus("ACTIVE");

        ResponseEntity<EnrollmentDto> createResponse = executePost(
                "/enrollments",
                createRequest,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        this.testEnrollmentId = createResponse.getBody().getId();

        // 2. Получаем все enrollments пользователя
        ResponseEntity<EnrollmentDto[]> getResponse = executeGet(
                "/enrollments/user/" + testUserId,
                EnrollmentDto[].class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertTrue(getResponse.getBody().length > 0);

        // Проверяем, что в списке есть наш enrollment
        boolean found = false;
        for (EnrollmentDto enrollment : getResponse.getBody()) {
            if (enrollment.getId().equals(testEnrollmentId)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Created enrollment should be in user's enrollments list");
    }

    @Test
    void testGetEnrollmentsByCourse() {
        // 1. Сначала создаем enrollment
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setStatus("ACTIVE");

        ResponseEntity<EnrollmentDto> createResponse = executePost(
                "/enrollments",
                createRequest,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        this.testEnrollmentId = createResponse.getBody().getId();

        // 2. Получаем все enrollments курса
        ResponseEntity<EnrollmentDto[]> getResponse = executeGet(
                "/enrollments/course/" + testCourseId,
                EnrollmentDto[].class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertTrue(getResponse.getBody().length > 0);
    }

    @Test
    void testUpdateEnrollmentStatus() {
        // 1. Создаем enrollment
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setStatus("ACTIVE");

        ResponseEntity<EnrollmentDto> createResponse = executePost(
                "/enrollments",
                createRequest,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        this.testEnrollmentId = createResponse.getBody().getId();

        // 2. Обновляем статус
        EnrollmentDto.UpdateRequest updateRequest = new EnrollmentDto.UpdateRequest();
        updateRequest.setStatus("COMPLETED");

        HttpEntity<EnrollmentDto.UpdateRequest> requestEntity = new HttpEntity<>(
                updateRequest,
                createHeaders(adminUsername, adminPassword, new ContentType(true, "application/json"))
        );

        ResponseEntity<EnrollmentDto> updateResponse = restTemplate.exchange(
                baseUrl + "/enrollments/" + testEnrollmentId + "/status",
                HttpMethod.PATCH,
                requestEntity,
                EnrollmentDto.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("COMPLETED", updateResponse.getBody().getStatus());
    }

    @Test
    void testDeleteEnrollment() {
        // 1. Создаем enrollment
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setStatus("ACTIVE");

        ResponseEntity<EnrollmentDto> createResponse = executePost(
                "/enrollments",
                createRequest,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Integer enrollmentId = createResponse.getBody().getId();

        // 2. Удаляем enrollment
        ResponseEntity<Void> deleteResponse = executeDelete(
                "/enrollments/" + enrollmentId,
                Void.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // 3. Пытаемся получить удаленный enrollment (должен вернуть 404)
        ResponseEntity<EnrollmentDto> getResponse = executeGet(
                "/enrollments/" + enrollmentId,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testDuplicateEnrollmentPrevention() {
        // 1. Создаем первое enrollment
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setStatus("ACTIVE");

        ResponseEntity<EnrollmentDto> firstResponse = executePost(
                "/enrollments",
                createRequest,
                EnrollmentDto.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // 2. Пытаемся создать дубликат (должен вернуть ошибку)
        ResponseEntity<String> secondResponse = executePost(
                "/enrollments",
                createRequest,
                String.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.BAD_REQUEST, secondResponse.getStatusCode());
        assertTrue(secondResponse.getBody().contains("already enrolled"));
    }

    @Test
    void testGetActiveEnrollmentsCount() {
        // Создаем несколько enrollments для теста
        EnrollmentDto.CreateRequest createRequest = new EnrollmentDto.CreateRequest();
        createRequest.setUserId(testUserId);
        createRequest.setCourseId(testCourseId);
        createRequest.setStatus("ACTIVE");

        executePost("/enrollments", createRequest, EnrollmentDto.class, adminUsername, adminPassword);

        // Получаем количество активных enrollments
        ResponseEntity<Long> countResponse = executeGet(
                "/enrollments/course/" + testCourseId + "/active-count",
                Long.class,
                adminUsername,
                adminPassword
        );

        assertEquals(HttpStatus.OK, countResponse.getStatusCode());
        assertNotNull(countResponse.getBody());
        assertTrue(countResponse.getBody() >= 0);
    }

    // Переопределяем методы execute* из BaseTest для работы с текущим restTemplate
    @Override
    protected <T> ResponseEntity<T> executeGet(String url, Class<T> responseType,
                                               String username, String password) {
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(username, password, null));
        return restTemplate.exchange(baseUrl + url, HttpMethod.GET, request, responseType);
    }

    @Override
    protected <T, R> ResponseEntity<T> executePost(String url, R body, Class<T> responseType,
                                                   String username, String password) {
        HttpEntity<R> request = new HttpEntity<>(body, createHeaders(username, password,
                new ContentType(true, "application/json")));
        return restTemplate.exchange(baseUrl + url, HttpMethod.POST, request, responseType);
    }

    @Override
    protected <T, R> ResponseEntity<T> executePut(String url, Class<T> responseType,
                                                  String username, String password) {
        HttpHeaders headers = createHeaders(username, password, null);
        HttpEntity<R> request = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.PUT, request, responseType);
    }

    @Override
    protected <T, R> ResponseEntity<T> executePut(String url, R body, Class<T> responseType,
                                                  String username, String password) {
        HttpEntity<R> request = new HttpEntity<>(body, createHeaders(username, password,
                new ContentType(true, "application/json")));
        return restTemplate.exchange(baseUrl + url, HttpMethod.PUT, request, responseType);
    }

    @Override
    protected <T, R> ResponseEntity<T> executeDelete(String url, Class<T> responseType,
                                                     String username, String password) {
        HttpHeaders headers = createHeaders(username, password, null);
        HttpEntity<R> request = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.DELETE, request, responseType);
    }
}