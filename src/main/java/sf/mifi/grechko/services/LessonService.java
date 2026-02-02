package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.models.Lesson;
import sf.mifi.grechko.models.Module;
import sf.mifi.grechko.repositories.LessonRepository;
import sf.mifi.grechko.repositories.ModuleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    @Transactional
    public LessonDto createLesson(CreateLessonRequest request) {
        // Проверяем, существует ли уже запись
        if (lessonRepository.existsByTitleAndModuleId(request.getTitle(), request.getModuleId())) {
            throw new IllegalArgumentException("Student already enrolled in this course");
        }

        Module module = moduleRepository.findByIdWithDetails(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Модуль с таким ID не существует"));

        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setModule(module);

        var result = lessonRepository.save(lesson);

        return LessonDto.fromEntity(result);
    }

    @Transactional
    public LessonDto updateLessonById(Integer lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        if (request.getTitle() != null)
            lesson.setTitle(request.getTitle());

        if (request.getContent() != null)
            lesson.setContent(request.getContent());

        if (request.getVideoUrl() != null)
            lesson.setVideoUrl(request.getVideoUrl());

        return LessonDto.fromEntity(lessonRepository.save(lesson));
    }

    public LessonDto getLessonById(Integer id) {
        return lessonRepository.findByIdWithDetails(id)
                .map(LessonDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Нет урока с таким ID"));
    }

    public List<LessonDto> getAll() {
        return lessonRepository.findAllWithDetails()
                .stream()
                .map(LessonDto::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteLesson(Integer id) {
        if (!lessonRepository.existsById(id)) {
            throw new IllegalArgumentException("Урок с таким ID не существует");
        }

        lessonRepository.deleteById(id);
    }


}
