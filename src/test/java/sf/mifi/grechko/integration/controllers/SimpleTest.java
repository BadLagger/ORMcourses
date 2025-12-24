package sf.mifi.grechko.integration.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SimpleTest {

    @Test
    void contextLoads() {
        System.out.println("Spring context loaded successfully!");
    }
}