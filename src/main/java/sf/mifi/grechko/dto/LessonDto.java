package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Lesson;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDto {
    private Integer id;
    private String title;
    private String content;
    private String videoUrl;
    private Integer moduleId;
    private String moduleTitle;
    //private List<AssignmentDto> assignments;

    public static LessonDto fromEntity(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .moduleId(lesson.getModule().getId())
                .moduleTitle(lesson.getModule().getTitle())
                .build();
    }
}
