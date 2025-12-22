package sf.mifi.grechko.dto;

import lombok.Data;
import sf.mifi.grechko.models.Profile;

import java.time.LocalDateTime;

@Data
public class ProfileDto {
    private Integer id;
    private Integer userId;
    private String userLogin;
    private String bio;
    private String avatarUrl;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструктор из сущности
    public static ProfileDto fromEntity(Profile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setUserLogin(profile.getUser().getLogin());
        dto.setBio(profile.getBio());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setEmail(profile.getEmail());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
