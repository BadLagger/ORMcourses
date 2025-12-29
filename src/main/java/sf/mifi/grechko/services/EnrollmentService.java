package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mifi.grechko.dto.EnrollmentDto;
import sf.mifi.grechko.mapper.EnrollmentMapper;
import sf.mifi.grechko.models.Course;
import sf.mifi.grechko.models.Enrollment;
import sf.mifi.grechko.models.User;
import sf.mifi.grechko.repositories.CourseRepository;
import sf.mifi.grechko.repositories.EnrollmentRepository;
import sf.mifi.grechko.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollmentById(Integer id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + id));
        return enrollmentMapper.toDTO(enrollment);
    }

    @Transactional
    public EnrollmentDto createEnrollment(EnrollmentDto.CreateRequest request) {
        // Проверяем, не зачислен ли уже пользователь на курс
        if (enrollmentRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new RuntimeException("User already enrolled in this course");
        }

        User user = userRepository.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        Enrollment enrollment = enrollmentMapper.toEntity(request);
        enrollment.setUser(user);
        enrollment.setCourse(course);

        // Устанавливаем статус, если не указан в запросе
        if (request.getStatus() != null) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(request.getStatus()));
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toDTO(savedEnrollment);
    }

    @Transactional
    public EnrollmentDto updateEnrollmentStatus(Integer id, EnrollmentDto.UpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + id));

        enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(request.getStatus()));
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        return enrollmentMapper.toDTO(updatedEnrollment);
    }

    @Transactional
    public void deleteEnrollment(Integer id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new RuntimeException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getEnrollmentsByUser(Integer userId) {
        return enrollmentRepository.findByUserIdWithCourses(userId)
                .stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getEnrollmentsByCourse(Integer courseId) {
        return enrollmentRepository.findByCourseIdWithUsers(courseId)
                .stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getActiveEnrollmentsCountByCourse(Integer courseId) {
        return enrollmentRepository.countByCourseIdAndStatus(
                courseId,
                Enrollment.EnrollmentStatus.ACTIVE
        );
    }
}
