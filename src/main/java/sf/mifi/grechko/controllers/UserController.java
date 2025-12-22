package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.services.UserService;
import sf.mifi.grechko.dto.CreateUserRequest;
import sf.mifi.grechko.dto.ChangePasswordRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить всех пользователей (только для ADMIN)")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @Operation(summary = "Создать нового пользователя (только для ADMIN)")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getLogin(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя (только для ADMIN)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Изменить роль пользователя (только для ADMIN)")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestParam User.Role role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Изменить пароль пользователя")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
