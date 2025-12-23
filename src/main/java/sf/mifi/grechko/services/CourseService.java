package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.CourseDto;
import sf.mifi.grechko.dto.CreateCourseRequest;
import sf.mifi.grechko.dto.UpdateCourseRequest;
import sf.mifi.grechko.models.Category;
import sf.mifi.grechko.models.Course;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.CategoryRepository;
import sf.mifi.grechko.repositories.CourseRepository;
import sf.mifi.grechko.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userContextService;

    /**
     * Получить все курсы (доступно всем)
     */
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAllWithDetails().stream()
                .map(CourseDto::fromEntity)
                .toList();
    }

    /**
     * Получить курс по ID (доступно всем)
     */
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Integer id) {
        return courseRepository.findByIdWithDetails(id)
                .map(CourseDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден: " + id));
    }

    /**
     * Создать курс
     * TEACHER: автоматически назначается как преподаватель
     * ADMIN: может указать любого преподавателя
     */
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request) {
        User currentUser = userContextService.getCurrentUser();

        // Проверяем категорию
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + request.getCategoryId()));

        // Определяем преподавателя
        User teacher;
        if (currentUser.getRole() == User.Role.ADMIN) {
            // ADMIN должен явно указать преподавателя
            if (request.getTeacherId() == null) {
                throw new IllegalArgumentException("Для администратора необходимо указать teacherId");
            }
            teacher = userRepository.findById(Long.valueOf(request.getTeacherId()))
                    .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден: " + request.getTeacherId()));

            if (teacher.getRole() != User.Role.TEACHER) {
                throw new IllegalArgumentException("Указанный пользователь не является преподавателем");
            }
        } else if (currentUser.getRole() == User.Role.TEACHER) {
            // TEACHER автоматически становится преподавателем курса
            teacher = currentUser;

            // TEACHER не может указать другого преподавателя
            if (request.getTeacherId() != null && !request.getTeacherId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Преподаватель может создавать курсы только за себя");
            }
        } else {
            throw new AccessDeniedException("Только преподаватели и администраторы могут создавать курсы");
        }

        // Проверяем уникальность названия курса у преподавателя
        if (courseRepository.existsByTitleAndTeacherId(request.getTitle(), teacher.getId())) {
            throw new IllegalArgumentException("У этого преподавателя уже есть курс с таким названием");
        }

        // Создаем курс
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(category);
        course.setTeacher(teacher);
        course.setDuration(request.getDuration());
        course.setStartDate(request.getStartDate());

        Course saved = courseRepository.save(course);
        return CourseDto.fromEntity(saved);
    }

    /**
     * Обновить курс
     * TEACHER: может обновлять только свои курсы
     * ADMIN: может обновлять любые курсы
     */
    @Transactional
    public CourseDto updateCourse(Integer courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден: " + courseId));

        User currentUser = userContextService.getCurrentUser();

        // Проверяем права
        if (currentUser.getRole() == User.Role.TEACHER &&
                !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете редактировать только свои курсы");
        }

        // Обновляем поля
        if (request.getTitle() != null) {
            // Проверяем уникальность названия у преподавателя
            if (!course.getTitle().equals(request.getTitle()) &&
                    courseRepository.existsByTitleAndTeacherId(request.getTitle(), course.getTeacher().getId())) {
                throw new IllegalArgumentException("У этого преподавателя уже есть курс с таким названием");
            }
            course.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + request.getCategoryId()));
            course.setCategory(category);
        }

        // Только ADMIN может менять преподавателя
        if (request.getTeacherId() != null && currentUser.getRole() == User.Role.ADMIN) {
            User newTeacher = userRepository.findById(Long.valueOf(request.getTeacherId()))
                    .orElseThrow(() -> new IllegalArgumentException("Преподаватель не найден: " + request.getTeacherId()));

            if (newTeacher.getRole() != User.Role.TEACHER) {
                throw new IllegalArgumentException("Новый преподаватель должен иметь роль TEACHER");
            }
            course.setTeacher(newTeacher);
        }

        if (request.getDuration() != null) {
            course.setDuration(request.getDuration());
        }

        if (request.getStartDate() != null) {
            course.setStartDate(request.getStartDate());
        }

        Course saved = courseRepository.save(course);
        return CourseDto.fromEntity(saved);
    }

    /**
     * Удалить курс
     * TEACHER: только свои курсы
     * ADMIN: любые курсы
     */
    @Transactional
    public void deleteCourse(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден: " + courseId));

        User currentUser = userContextService.getCurrentUser();

        // Проверяем права
        if (currentUser.getRole() == User.Role.TEACHER &&
                !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете удалять только свои курсы");
        }

        courseRepository.delete(course);
    }

    /**
     * Получить курсы преподавателя
     */
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByTeacher(Integer teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(CourseDto::fromEntity)
                .toList();
    }

    /**
     * Получить курсы категории
     */
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByCategory(Integer categoryId) {
        return courseRepository.findByCategoryId(categoryId).stream()
                .map(CourseDto::fromEntity)
                .toList();
    }

    /**
     * Получить мои курсы (для преподавателя)
     */
    @Transactional(readOnly = true)
    public List<CourseDto> getMyCourses() {
        User currentUser = userContextService.getCurrentUser();
        if (currentUser.getRole() != User.Role.TEACHER) {
            throw new AccessDeniedException("Только преподаватели могут просматривать свои курсы");
        }

        return courseRepository.findByTeacherId(currentUser.getId()).stream()
                .map(CourseDto::fromEntity)
                .toList();
    }
}
