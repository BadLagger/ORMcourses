package sf.mifi.grechko.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCourseRequest {
    private String title;
    private String description;
    private Integer categoryId;
    private Integer teacherId;  // Только для ADMIN
    private String duration;
    private LocalDate startDate;
}