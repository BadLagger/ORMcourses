package sf.mifi.grechko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLessonRequest {
    private String title;
    private String content;
    private String videoUrl;
}
