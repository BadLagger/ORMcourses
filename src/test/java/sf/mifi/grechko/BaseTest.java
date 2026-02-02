package sf.mifi.grechko;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Base64;
import java.util.Map;

public abstract class BaseTest {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected static final String BASE_HOST_URL = "http://localhost:";

    protected static final String AdminUsername="admin";
    protected static final String AdminPassword="admin123";
    protected static String TeacherUsername="teacher";
    protected static String TeacherPassword="teacher123";
    protected static final String Teacher2Username="teacher2";
    protected static String Teacher2Password="teacher1232";
    protected static String UserUsername="user";
    protected static String UserPassword="user123";
    protected static String TestCategoryName="Java";
    protected static Integer TestCategoryId = 0;
    protected static Integer TestTeacherId = 0;
    protected static Integer TestTeacher2Id = 0;
    protected static Integer TestUserId = 0;
    protected static Integer TestCourseId = 0;
    protected static Integer TestModuleId = 0;
    protected static Integer TestLessonId = 0;

    protected static TestRestTemplate restTemplate;
    protected static String baseUrl;

    protected static class ContentType {
        public boolean Usage;
        public String Type;

        public ContentType(boolean usage, String type) {
            this.Usage = usage;
            this.Type = type;
        }
    }

    protected static HttpHeaders createHeaders(String username, String password, ContentType contentType) {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        if (contentType != null && contentType.Usage) {
            headers.set("Content-Type", contentType.Type);
        }

        return headers;
    }

    protected <T> ResponseEntity<T> executeGet(String url, Class<T> responseType,
                                               String username, String password) {
            HttpEntity<Void> request = new HttpEntity<>(getHeader(username, password, null));
            return restTemplate.exchange(baseUrl + url, HttpMethod.GET, request, responseType);
    }

    protected static <T, R> ResponseEntity<T> executePost(String url, R body, Class<T> responseType,
                                                   String username, String password) {
        HttpEntity<R> request = new HttpEntity<>(body, getHeader(username, password, new ContentType(true, "application/json")));
        return restTemplate.exchange(baseUrl + url, HttpMethod.POST, request, responseType);
    }

    protected <T, R> ResponseEntity<T> executePut(String url, Class<T> responseType,
                                                  String username, String password) {
        HttpHeaders headers = createHeaders(username, password, null);
        HttpEntity<R> request = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.PUT, request, responseType);
    }

    protected <T, R> ResponseEntity<T> executePut(String url, R body, Class<T> responseType,
                                                  String username, String password) {
        HttpEntity<R> request = new HttpEntity<>(body, getHeader(username, password, new ContentType(true, "application/json")));
        return restTemplate.exchange(baseUrl + url, HttpMethod.PUT, request, responseType);
    }

    protected static <T, R> ResponseEntity<T> executeDelete(String url, Class<T> responseType, String username, String password) {
        HttpHeaders headers = createHeaders(username, password, null);
        HttpEntity<R> request = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.DELETE, request, responseType);
    }

    private static HttpHeaders getHeader(String username, String password, ContentType contentType) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            return createHeaders(username, password, contentType);
        }
        return new HttpHeaders();
    }

    protected static boolean createTestUsersViaApi() {
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
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestTeacherId = Integer.valueOf(responseBody.get("id").toString());

            response = executePost("/api/users", userRequest,
                    String.class, AdminUsername, AdminPassword);

            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestUserId = Integer.valueOf(responseBody.get("id").toString());

        } catch (Exception exp) {
            return false;
        }
        return true;
    }

    protected static boolean createAdditionalUsersViaApi() {
        Map<String, Object> teacherRequest = Map.of(
                "login", Teacher2Username,
                "password", Teacher2Password,
                "role", "TEACHER"
        );

        try {
            ResponseEntity<String> response = executePost("/api/users", teacherRequest,
                    String.class, AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestTeacher2Id = Integer.valueOf(responseBody.get("id").toString());
        } catch (Exception exp) {
            return false;
        }
        return true;
    }

    protected static boolean deleteTestUsersViaApi() {
        String teacherEndpoint = "/api/users" + "/" + TestTeacherId;
        String userEndpoint = "/api/users" + "/" + TestUserId;

        try {
            ResponseEntity<String> response = executeDelete(teacherEndpoint, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                return false;
            }

            response = executeDelete(userEndpoint, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected static boolean deleteAdditionUsersViaApi() {
        String teacherEndpoint = "/api/users" + "/" + TestTeacher2Id;
        try {
            ResponseEntity<String> response = executeDelete(teacherEndpoint, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected static boolean createTestCategoryViaApi() {
        Map<String, Object> request = Map.of(
                "name", TestCategoryName
        );

        try {
            ResponseEntity<String> response = executePost("/api/categories", request, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestCategoryId = Integer.valueOf(responseBody.get("id").toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected static boolean createTestCourseViaApi() {
        Map<String, Object> request = Map.of(
                "title", "REST API with Java",
                "description", "test course",
                "categoryId", TestCategoryId,
                "duration", "5 weeks"
        );

        try {
            ResponseEntity<String> response = executePost("/api/courses", request, String.class,
                    TeacherUsername, TeacherPassword);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestCourseId = Integer.valueOf(responseBody.get("id").toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected static boolean createTestModuleViaApi() {
        Map<String, Object> request = Map.of(
                "title", "Introduction",
                "description", "Test description",
                "courseId", TestCourseId
        );

        try {
            ResponseEntity<String> response = executePost("/api/modules", request, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestModuleId = Integer.valueOf(responseBody.get("id").toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected static boolean createTestLessonViaApi() {

        Map<String, Object> request = Map.of(
                "title", "Test Lesson",
                "content", "Test Content",
                "videoUrl", "http://my.lesson",
                "moduleId", TestModuleId
        );

        try {
            ResponseEntity<String> response = executePost("/api/lessons", request, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            TestLessonId = Integer.valueOf(responseBody.get("id").toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }



    protected static boolean deleteTestCategoriesViaApi() {
        String endpoint = "/api/categories" + "/" + TestCategoryId;

        try {
            ResponseEntity<String> response = executeDelete(endpoint, String.class,
                    AdminUsername, AdminPassword);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
         return true;
    }
}
