package sf.mifi.grechko.dto;

import lombok.Builder;
import lombok.Data;
import sf.mifi.grechko.models.User;

@Data
@Builder
public class UserDto {
    private Integer id;
    private String login;
    private User.Role role;
}
