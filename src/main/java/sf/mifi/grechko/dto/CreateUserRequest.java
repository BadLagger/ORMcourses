package sf.mifi.grechko.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import sf.mifi.grechko.models.User;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    @NotBlank(message = "Роль не может быть пустым")
    private User.Role role;
}
