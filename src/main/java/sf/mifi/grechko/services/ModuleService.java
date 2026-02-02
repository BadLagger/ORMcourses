package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.models.Course;
import sf.mifi.grechko.models.Enrollment;
import sf.mifi.grechko.models.Module;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.CourseRepository;
import sf.mifi.grechko.repositories.ModuleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public ModuleDto createModule(CreateModuleRequest request) {
        // Проверяем, существует ли уже запись
        if (moduleRepository.existsByTitleAndCourseId(request.getTitle(), request.getCourseId())) {
            throw new IllegalArgumentException("Такой модуль уже существует");
        }

        var orderIndex = request.getOrderIndex();

        if (orderIndex != null) {
            if (moduleRepository.existsByOrderIndex(orderIndex)) {
                throw new IllegalArgumentException("Модуль с таким порядковым номером уже существует");
            }
        } else {
            orderIndex = moduleRepository.getNextOrderIndex(request.getCourseId());
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Module module = new Module();
        module.setTitle(request.getTitle());
        module.setOrderIndex(orderIndex);
        module.setDescription(request.getDescription());
        module.setCourse(course);

        var result = moduleRepository.save(module);
        return ModuleDto.fromEntity(result);
    }

    @Transactional
    public ModuleDto updateModuleById(Integer moduleId, UpdateModuleRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));

        if (request.getTitle() != null)
            module.setTitle(request.getTitle());

        if (request.getDescription() != null)
            module.setDescription(request.getDescription());

        if (request.getOrderIndex() != null)
            module.setOrderIndex(request.getOrderIndex());

        return ModuleDto.fromEntity(moduleRepository.save(module));
    }


    public List<ModuleDto> getAll() {
        return moduleRepository.findAllWithDetails()
                .stream()
                .map(ModuleDto::fromEntity)
                .toList();
    }

    public ModuleDto getModuleById(Integer id) {
        return moduleRepository.findByIdWithDetails(id)
                .map(ModuleDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Модуль с таким Id не найдена"));
    }

    @Transactional
    public void deleteModule(Integer id) {
        if (!moduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Модуль таким ID не существует");
        }

        moduleRepository.deleteById(id);
    }
}
