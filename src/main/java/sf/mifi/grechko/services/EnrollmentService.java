package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.CourseDto;
import sf.mifi.grechko.dto.CreateEnrollmentRequest;
import sf.mifi.grechko.dto.EnrollmentDto;
import sf.mifi.grechko.dto.UpdateEnrollmentRequest;
import sf.mifi.grechko.models.Course;
import sf.mifi.grechko.models.Enrollment;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.CourseRepository;
import sf.mifi.grechko.repositories.EnrollmentRepository;
import sf.mifi.grechko.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserService userContextService;

    @Transactional
    public EnrollmentDto createEnrollment(CreateEnrollmentRequest request) {
        // Проверяем, существует ли уже запись
        if (enrollmentRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new IllegalArgumentException("Student already enrolled in this course");
        }

        User student = userRepository.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Проверяем, что пользователь не является преподавателем этого курса
        if (course.getTeacher().getId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Teacher cannot enroll in own course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(student);
        enrollment.setCourse(course);
        if (request.getStatus() == null)
            enrollment.setStatus(Enrollment.Status.ACTIVE);
        else
            enrollment.setStatus(request.getStatus());
        enrollment.setEnrollDate(LocalDateTime.now());

        var result = enrollmentRepository.save(enrollment);

        return EnrollmentDto.fromEntity(result);
    }

    @Transactional
    public EnrollmentDto updateEnrollmentStatus(Integer enrollmentId, UpdateEnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        enrollment.setStatus(request.getStatus());

        return EnrollmentDto.fromEntity(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void cancelEnrollment(Integer userId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        enrollment.setStatus(Enrollment.Status.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    public List<EnrollmentDto> getAll() {
        return enrollmentRepository.findAllWithDetails()
                .stream()
                .map(EnrollmentDto::fromEntity)
                .toList();
    }

    public List<Enrollment> getStudentEnrollments(Integer userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    public List<Enrollment> getCourseEnrollments(Integer courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    public EnrollmentDto getCourseEnrollmentsById(Integer id) {
        return enrollmentRepository.findByIdWithDetails(id)
                .map(EnrollmentDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Связь с таким Id не найдена"));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getMyEnrollments() {
        User currentUser = userContextService.getCurrentUser();

        var result = enrollmentRepository.findByUserId(currentUser.getId()).stream()
                .map(EnrollmentDto::fromEntity)
                .toList();

        if (!result.isEmpty())
            return result;
        else
            throw new IllegalArgumentException("У пользователя нет связей");
    }

    @Transactional
    public void deleteEnrollment(Integer id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Связь с таким ID не существует");
        }

        enrollmentRepository.deleteById(id);
    }


}
