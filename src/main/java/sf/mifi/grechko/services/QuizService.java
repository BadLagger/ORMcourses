package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.*;
import sf.mifi.grechko.models.Module;
import sf.mifi.grechko.models.Quiz;
import sf.mifi.grechko.repositories.ModuleRepository;
import sf.mifi.grechko.repositories.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;

    @Transactional
    public QuizDto createQuiz(CreateQuizRequest request) {
        // Проверяем, существует ли уже тест
        if (quizRepository.existsByTitleAndModuleId(request.getTitle(), request.getModuleId())) {
            throw new IllegalArgumentException("Quiz already exists!");
        }

        Module module = moduleRepository.findByIdWithDetails(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Модуль с таким ID не существует"));

        Quiz quiz = new Quiz();

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setModule(module);
        quiz.setIsPublished(false);
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setPassingScore(request.getPassingScore());

        var result = quizRepository.save(quiz);

        return QuizDto.fromEntity(result);
    }

    @Transactional
    public QuizDto updateQuizById(Integer quizId, UpdateQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        if (request.getTitle() != null)
            quiz.setTitle(request.getTitle());

        if (request.getDescription() != null)
            quiz.setDescription(request.getDescription());

        if (request.getIsPublished() != null)
            quiz.setIsPublished(request.getIsPublished());

        if (request.getMaxAttempts() != null)
            quiz.setMaxAttempts(request.getMaxAttempts());

        if (request.getPassingScore() != null)
            quiz.setPassingScore(request.getPassingScore());

        if (request.getTimeLimitMinutes() != null)
            quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());

        return QuizDto.fromEntity(quizRepository.save(quiz));
    }

    public QuizDto getQuizById(Integer id) {
        return quizRepository.findByIdWithDetails(id)
                .map(QuizDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Нет теста с таким ID"));
    }

    public List<QuizDto> getAll() {
        return quizRepository.findAllWithDetails()
                .stream()
                .map(QuizDto::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteQuiz(Integer id) {
        if (!quizRepository.existsById(id)) {
            throw new IllegalArgumentException("Урок с таким ID не существует");
        }

        quizRepository.deleteById(id);
    }
}
