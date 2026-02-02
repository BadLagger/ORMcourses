package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sf.mifi.grechko.dto.LessonDto;
import sf.mifi.grechko.dto.SubmissionDto;
import sf.mifi.grechko.services.SubmissionService;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping
    @Operation(summary = "Получить все ответы (доступно всем авторизированным)")
    public ResponseEntity<List<SubmissionDto>> getAllLessons() {
        return ResponseEntity.ok(submissionService.getAll());
    }
}
