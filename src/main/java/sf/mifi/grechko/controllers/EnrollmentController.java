package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.services.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @GetMapping
    @Operation(summary = "Получить все связи пользователей с курсами (доступно всем)")
    public ResponseEntity<List<EnrollmentDto>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAll());
    }

    @PostMapping
    @Operation(summary = "Создать связь пользователя с курсом (только ADMIN)")
    public ResponseEntity<EnrollmentDto> createEnrollment(
            @Valid @RequestBody CreateEnrollmentRequest request) {
        try {
            var result = enrollmentService.createEnrollment(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить связь по ID (доступно всем)")
    public ResponseEntity<EnrollmentDto> getEnrollmentById(@PathVariable Integer id) {
        try {
            var result = enrollmentService.getCourseEnrollmentsById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Получить свои связи (только авторизированным)")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments() {
        try {
            var result = enrollmentService.getMyEnrollments();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить статус связи (только ADMIN)")
    public ResponseEntity<EnrollmentDto> updateEnrollmentById(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEnrollmentRequest request) {
        try {
            var result = enrollmentService.updateEnrollmentStatus(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить связь (только ADMIN)")
    public ResponseEntity<Void> deleteEnrollmentById(@PathVariable Integer id) {
        try {
            enrollmentService.deleteEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
