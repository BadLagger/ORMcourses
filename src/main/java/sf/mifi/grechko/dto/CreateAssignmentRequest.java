package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private Integer lessonId;
}
