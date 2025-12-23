package sf.mifi.grechko.dto;

import lombok.Data;
import sf.mifi.grechko.models.Category;

import java.time.LocalDateTime;

@Data
public class CategoryDto {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;

    public static CategoryDto fromEntity(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
}