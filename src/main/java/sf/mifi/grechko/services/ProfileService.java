package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.ProfileDto;
import sf.mifi.grechko.models.Profile;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.ProfileRepository;
import sf.mifi.grechko.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /**
     * Получить профиль пользователя по ID пользователя
     * Доступно всем аутентифицированным пользователям
     */
    @Transactional(readOnly = true)
    public Optional<ProfileDto> getProfileByUserId(Integer userId) {
        return profileRepository.findByUserIdWithUser(userId)
                .map(ProfileDto::fromEntity);
    }

    /**
     * Получить свой профиль
     */
    @Transactional(readOnly = true)
    public Optional<ProfileDto> getMyProfile(Integer currentUserId) {
        return getProfileByUserId(currentUserId);
    }

    /**
     * Создать или обновить профиль
     * Только владелец профиля
     */
    @Transactional
    public ProfileDto createOrUpdateProfile(Integer userId, ProfileDto profileDto) {
        // Проверяем существование пользователя
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Проверяем email на уникальность (если он есть)
        if (profileDto.getEmail() != null && !profileDto.getEmail().isBlank()) {
            profileRepository.findByEmail(profileDto.getEmail())
                    .ifPresent(existingProfile -> {
                        if (!existingProfile.getUser().getId().equals(userId)) {
                            throw new IllegalArgumentException("Email уже используется другим пользователем");
                        }
                    });
        }

        // Ищем существующий профиль
        Profile profile = profileRepository.findByUserId(userId)
                .orElse(new Profile());

        // Обновляем поля
        profile.setUser(user);
        profile.setBio(profileDto.getBio());
        profile.setAvatarUrl(profileDto.getAvatarUrl());
        profile.setEmail(profileDto.getEmail());

        Profile saved = profileRepository.save(profile);
        return ProfileDto.fromEntity(saved);
    }

    /**
     * Удалить профиль (например, при удалении пользователя каскадно)
     * Только для администратора
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteProfile(Integer userId) {
        profileRepository.findByUserId(userId)
                .ifPresent(profileRepository::delete);
    }

    /**
     * Проверка прав доступа
     */
    public void checkProfileAccess(Integer currentUserId, Integer targetUserId) {
        if (!currentUserId.equals(targetUserId)) {
            // Можно расширить: админы могут редактировать любые профили
            throw new AccessDeniedException("Нет прав доступа к профилю");
        }
    }
}
