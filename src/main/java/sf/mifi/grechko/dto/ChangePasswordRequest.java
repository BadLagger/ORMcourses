package sf.mifi.grechko.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Текущий пароль не может быть пустым")
    private String currentPassword;

    @NotBlank(message = "Новый пароль не может быть пустым")
    private String newPassword;
}
