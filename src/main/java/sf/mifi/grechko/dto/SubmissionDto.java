package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Lesson;
import sf.mifi.grechko.models.Submission;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDto {
    private Integer id;
    private Integer assignmentId;
    private String assignmentTitle;
    private Integer studentId;
    private String studentLogin;
    private LocalDateTime submittedAt;
    private String content;
    private Integer score;
    private String feedback;
    private Submission.Status status;

    public static SubmissionDto fromEntity(Submission submission) {
        return SubmissionDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentLogin(submission.getStudent().getLogin())
                .submittedAt(submission.getSubmittedAt())
                .content(submission.getContent())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .status(submission.getStatus())
                .build();
    }
}
