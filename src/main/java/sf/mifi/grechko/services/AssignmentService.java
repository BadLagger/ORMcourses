package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.models.Assignment;
import sf.mifi.grechko.models.Lesson;
import sf.mifi.grechko.repositories.AssignmentRepository;
import sf.mifi.grechko.repositories.LessonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public AssignmentDto createAssignment(CreateAssignmentRequest request) {
        // Проверяем, существует ли уже запись
        if (assignmentRepository.existsByTitleAndLessonId(request.getTitle(), request.getLessonId())) {
            throw new IllegalArgumentException("Задание с таким ID уже существует");
        }

        Lesson lesson = lessonRepository.findByIdWithDetails(request.getLessonId())
                .orElseThrow(()-> new IllegalArgumentException("Задание с таким ID не найдено"));

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxScore(request.getMaxScore());
        assignment.setLesson(lesson);

        var result = assignmentRepository.save(assignment);
        return AssignmentDto.fromEntity(result);

    }

    @Transactional
    public AssignmentDto updateAssignmentById(Integer id, UpdateAssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (request.getTitle() != null)
            assignment.setTitle(request.getTitle());

        if (request.getDescription() != null)
            assignment.setDescription(request.getDescription());

        if (request.getMaxScore() != null)
            assignment.setMaxScore(request.getMaxScore());

        if (request.getDueDate() != null)
            assignment.setDueDate(request.getDueDate());

        return AssignmentDto.fromEntity(assignmentRepository.save(assignment));
    }

    public AssignmentDto getAssignmentById(Integer id) {
        return assignmentRepository.findByIdWithDetails(id)
                .map(AssignmentDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Нет задания с таким ID"));
    }

    public List<AssignmentDto> getAll() {
        return assignmentRepository.findAllWithDetails()
                .stream()
                .map(AssignmentDto::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteAssignment(Integer id) {
        if (!assignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Задание с таким ID не существует");
        }

        assignmentRepository.deleteById(id);
    }
}
