package sf.mifi.grechko.dto;

import lombok.Data;
import sf.mifi.grechko.models.Course;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CourseDto {
    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private Integer teacherId;
    private String teacherLogin;
    private String duration;
    private LocalDate startDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseDto fromEntity(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());

        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
            dto.setCategoryName(course.getCategory().getName());
        }

        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherLogin(course.getTeacher().getLogin());
        }

        dto.setDuration(course.getDuration());
        dto.setStartDate(course.getStartDate());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());

        return dto;
    }
}
