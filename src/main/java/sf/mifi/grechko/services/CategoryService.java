package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.CategoryDto;
import sf.mifi.grechko.models.Category;
import sf.mifi.grechko.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .map(CategoryDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));
    }

    @Transactional
    public CategoryDto createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Категория с названием '" + name + "' уже существует");
        }

        Category category = new Category();
        category.setName(name);

        Category saved = categoryRepository.save(category);
        return CategoryDto.fromEntity(saved);
    }

    @Transactional
    public CategoryDto updateCategory(Integer id, String newName) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Категория с названием '" + newName + "' уже существует");
        }

        category.setName(newName);
        Category saved = categoryRepository.save(category);
        return CategoryDto.fromEntity(saved);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        // Проверяем, есть ли курсы в этой категории
        if (!category.getCourses().isEmpty()) {
            throw new IllegalStateException("Нельзя удалить категорию, к которой привязаны курсы");
        }

        categoryRepository.delete(category);
    }
}
