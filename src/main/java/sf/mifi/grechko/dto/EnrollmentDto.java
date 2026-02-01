package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Enrollment;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDto {
    private Integer id;
    private Integer userId;
    private String userLogin;
    private Integer courseId;
    private String courseTitle;
    private LocalDateTime enrollDate;
    private Enrollment.Status status;

    public static EnrollmentDto fromEntity(Enrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .userLogin(enrollment.getUser().getLogin())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .enrollDate(enrollment.getEnrollDate())
                .status(enrollment.getStatus())
                .build();
    }
}
