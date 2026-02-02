package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateModuleRequest {
    private String title;
    private String description;
    private Integer courseId;
    private Integer orderIndex; // опционально
}
