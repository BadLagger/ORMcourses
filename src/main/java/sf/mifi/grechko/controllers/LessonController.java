package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.services.LessonService;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class LessonController {
    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Получить все уроки (доступно всем авторизированным)")
    public ResponseEntity<List<LessonDto>> getAllLessons() {
        return ResponseEntity.ok(lessonService.getAll());
    }

    @PostMapping
    @Operation(summary = "Создать урок (ADMIN и TEACHER)")
    public ResponseEntity<LessonDto> createLesson(
            @Valid @RequestBody CreateLessonRequest request) {
        try {
            var result = lessonService.createLesson(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить урок по ID (доступно всем авторизированным)")
    public ResponseEntity<LessonDto> getLessonById(@PathVariable Integer id) {
        try {
            var result = lessonService.getLessonById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить урок по ID (ADMIN и TEACHER)")
    public ResponseEntity<LessonDto> updateLessonById(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateLessonRequest request) {
        try {
            var result = lessonService.updateLessonById(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить урок (ADMIN и TEACHER)")
    public ResponseEntity<Void> deleteLessonById(@PathVariable Integer id) {
        try {
            lessonService.deleteLesson(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
