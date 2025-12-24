package sf.mifi.grechko;

import org.jboss.jandex.TypeTarget;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Base64;

public abstract class BaseTest {

    protected TestRestTemplate restTemplate;
    protected String baseUrl;

    protected static class ContentType {
        public boolean Usage;
        public String Type;

        public ContentType(boolean usage, String type) {
            this.Usage = usage;
            this.Type = type;
        }
    }

    protected HttpHeaders createHeaders(String username, String password, ContentType contentType) {
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

    protected <T, R> ResponseEntity<T> executePost(String url, R body, Class<T> responseType,
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

    protected <T, R> ResponseEntity<T> executeDelete(String url, Class<T> responseType, String username, String password) {
        HttpHeaders headers = createHeaders(username, password, null);
        HttpEntity<R> request = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + url, HttpMethod.DELETE, request, responseType);
    }

    private HttpHeaders getHeader(String username, String password, ContentType contentType) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            return createHeaders(username, password, contentType);
        }
        return new HttpHeaders();
    }
}
