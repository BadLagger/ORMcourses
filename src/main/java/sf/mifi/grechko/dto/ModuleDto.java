package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sf.mifi.grechko.models.Module;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDto {
    private Integer id;
    private String title;
    private Integer orderIndex;
    private String description;
    private Integer courseId;
    private String courseTitle;
    /*private List<LessonDto> lessons;
    private QuizDto quiz;*/
    public static ModuleDto fromEntity(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .description(module.getDescription())
                .courseId(module.getCourse().getId())
                .courseTitle(module.getCourse().getTitle())
                .build();
    }

}
