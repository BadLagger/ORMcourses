package sf.mifi.grechko.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sf.mifi.grechko.dto.ProfileDto;
import sf.mifi.grechko.dto.UpdateProfileRequest;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.services.ProfileService;
import sf.mifi.grechko.services.UserService;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    /**
     * Получить свой профиль
     * Требуется аутентификация
     */
    @GetMapping("/me")
    @Operation(summary = "Получить свой профиль")
    public ResponseEntity<ProfileDto> getMyProfile() {

        User currentUser = userService.getCurrentUser();

        return profileService.getMyProfile(currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить профиль другого пользователя
     * Доступно всем аутентифицированным
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить профиль пользователя по ID")
    public ResponseEntity<ProfileDto> getProfileByUserId(
            @PathVariable Integer userId) {
        // Все аутентифицированные могут смотреть профили
        return profileService.getProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать или обновить свой профиль
     * Только владелец
     */
    @PutMapping("/me")
    @Operation(summary = "Создать или обновить свой профиль")
    public ResponseEntity<ProfileDto> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        User currentUser = userService.getCurrentUser();

        ProfileDto profileDto = new ProfileDto();
        profileDto.setBio(request.getBio());
        profileDto.setAvatarUrl(request.getAvatarUrl());
        profileDto.setEmail(request.getEmail());

        ProfileDto updated = profileService.createOrUpdateProfile(
                currentUser.getId(), profileDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Обновить профиль другого пользователя
     * Только ADMIN
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{userId}")
    @Operation(summary = "Обновить профиль пользователя (только ADMIN)")
    public ResponseEntity<ProfileDto> updateUserProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        ProfileDto profileDto = new ProfileDto();
        profileDto.setBio(request.getBio());
        profileDto.setAvatarUrl(request.getAvatarUrl());
        profileDto.setEmail(request.getEmail());

        ProfileDto updated = profileService.createOrUpdateProfile(userId, profileDto);
        return ResponseEntity.ok(updated);
    }
}
