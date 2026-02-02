package sf.mifi.grechko.dto;

import lombok.Data;

@Data
public class UpdateQuizRequest {
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Boolean isPublished;
}
