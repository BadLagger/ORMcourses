package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Enrollment;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentRequest {
    private Integer userId;
    private Integer courseId;
    private Enrollment.Status status; // Опционально
}
