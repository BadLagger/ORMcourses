package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuizRequest {
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Integer moduleId;
}
