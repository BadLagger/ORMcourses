package sf.mifi.grechko.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sf.mifi.grechko.dto.EnrollmentDto;
import sf.mifi.grechko.services.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<List<EnrollmentDto>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDto> getEnrollmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDto>> getEnrollmentsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByUser(userId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDto>> getEnrollmentsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @GetMapping("/course/{courseId}/active-count")
    public ResponseEntity<Long> getActiveEnrollmentsCount(@PathVariable Integer courseId) {
        return ResponseEntity.ok(enrollmentService.getActiveEnrollmentsCountByCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentDto> createEnrollment(
            @Valid @RequestBody EnrollmentDto.CreateRequest request) {
        EnrollmentDto created = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EnrollmentDto> updateEnrollmentStatus(
            @PathVariable Integer id,
            @Valid @RequestBody EnrollmentDto.UpdateRequest request) {
        return ResponseEntity.ok(enrollmentService.updateEnrollmentStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Integer id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
