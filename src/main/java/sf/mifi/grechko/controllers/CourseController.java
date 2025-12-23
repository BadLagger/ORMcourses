package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.CourseDto;
import sf.mifi.grechko.dto.CreateCourseRequest;
import sf.mifi.grechko.dto.UpdateCourseRequest;
import sf.mifi.grechko.services.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Получить все курсы (доступно всем)")
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить курс по ID (доступно всем)")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Integer id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Получить курсы преподавателя (доступно всем)")
    public ResponseEntity<List<CourseDto>> getCoursesByTeacher(@PathVariable Integer teacherId) {
        return ResponseEntity.ok(courseService.getCoursesByTeacher(teacherId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Получить курсы категории (доступно всем)")
    public ResponseEntity<List<CourseDto>> getCoursesByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(categoryId));
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои курсы (только TEACHER)")
    public ResponseEntity<List<CourseDto>> getMyCourses() {
        return ResponseEntity.ok(courseService.getMyCourses());
    }

    @PostMapping
    @Operation(summary = "Создать курс (TEACHER или ADMIN)")
    public ResponseEntity<CourseDto> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {
        CourseDto course = courseService.createCourse(request);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить курс (TEACHER - свои, ADMIN - все)")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseDto updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить курс (TEACHER - свои, ADMIN - все)")
    public ResponseEntity<Void> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}