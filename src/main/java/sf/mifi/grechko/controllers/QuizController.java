package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.services.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class QuizController {
    private final QuizService quizService;

    @GetMapping
    @Operation(summary = "Получить все тесты (доступно всем авторизированным)")
    public ResponseEntity<List<QuizDto>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAll());
    }

    @PostMapping
    @Operation(summary = "Создать тест (ADMIN и TEACHER)")
    public ResponseEntity<QuizDto> createQuiz(
            @Valid @RequestBody CreateQuizRequest request) {
        try {
            var result = quizService.createQuiz(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить тест по ID (доступно всем авторизированным)")
    public ResponseEntity<QuizDto> getQuizById(@PathVariable Integer id) {
        try {
            var result = quizService.getQuizById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить тест по ID (ADMIN и TEACHER)")
    public ResponseEntity<QuizDto> updateQuizById(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateQuizRequest request) {
        try {
            var result = quizService.updateQuizById(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тесты (ADMIN и TEACHER)")
    public ResponseEntity<Void> deleteQuizById(@PathVariable Integer id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
