package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.services.AssignmentService;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping
    @Operation(summary = "Получить все задания (доступно всем авторизированным)")
    public ResponseEntity<List<AssignmentDto>> getAllAssignment() {
        return ResponseEntity.ok(assignmentService.getAll());
    }

    @PostMapping
    @Operation(summary = "Создать задание (ADMIN и TEACHER)")
    public ResponseEntity<AssignmentDto> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request) {
        try {
            var result = assignmentService.createAssignment(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить задание по ID (доступно всем авторизированным)")
    public ResponseEntity<AssignmentDto> getAssignmentById(@PathVariable Integer id) {
        try {
            var result = assignmentService.getAssignmentById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить задание по ID (ADMIN и TEACHER)")
    public ResponseEntity<AssignmentDto> updateAssignmentById(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        try {
            var result = assignmentService.updateAssignmentById(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить задание (ADMIN и TEACHER)")
    public ResponseEntity<Void> deleteAssignmentById(@PathVariable Integer id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
