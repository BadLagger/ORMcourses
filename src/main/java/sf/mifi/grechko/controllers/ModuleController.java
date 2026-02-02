package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.services.ModuleService;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class ModuleController {
    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "Получить все модули (доступно всем)")
    public ResponseEntity<List<ModuleDto>> getAllEnrollments() {
        return ResponseEntity.ok(moduleService.getAll());
    }

    @PostMapping
    @Operation(summary = "Создать модуль (ADMIN и TEACHER)")
    public ResponseEntity<ModuleDto> createModule(
            @Valid @RequestBody CreateModuleRequest request) {
        try {
            var result = moduleService.createModule(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить модуль по ID (доступно всем)")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable Integer id) {
        try {
            var result = moduleService.getModuleById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить модуль (ADMIN и TEACHER)")
    public ResponseEntity<ModuleDto> updateModuleById(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateModuleRequest request) {
        try {
            var result = moduleService.updateModuleById(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить модуль (только ADMIN)")
    public ResponseEntity<Void> deleteModuleById(@PathVariable Integer id) {
        try {
            moduleService.deleteModule(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
