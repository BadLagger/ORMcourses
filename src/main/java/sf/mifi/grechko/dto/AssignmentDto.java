package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Assignment;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDto {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private Integer lessonId;
    private String lessonTitle;

    public static AssignmentDto fromEntity(Assignment assignment) {
        return AssignmentDto.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .lessonId(assignment.getLesson().getId())
                .lessonTitle(assignment.getLesson().getTitle())
                .build();
    }
}
