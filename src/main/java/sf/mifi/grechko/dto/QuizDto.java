package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Quiz;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDto  {
    private Integer id;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Boolean isPublished;
    private Integer moduleId;
    private String moduleTitle;

    public static QuizDto fromEntity(Quiz quiz) {
        return QuizDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .maxAttempts(quiz.getMaxAttempts())
                .isPublished(quiz.getIsPublished())
                .moduleId(quiz.getModule().getId())
                .moduleTitle(quiz.getModule().getTitle())
                .build();
    }
}
