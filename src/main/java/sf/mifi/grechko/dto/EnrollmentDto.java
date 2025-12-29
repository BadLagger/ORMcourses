package sf.mifi.grechko.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EnrollmentDto {
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private String courseTitle;
    private LocalDate enrollDate;
    private String status;

    @Data
    public static class CreateRequest {
        private Integer userId;
        private Integer courseId;
        private LocalDate enrollDate;
        private String status;
    }

    @Data
    public static class UpdateRequest {
        private String status;
    }
}