package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Enrollment;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    List<Enrollment> findByUserId(Integer userId);

    List<Enrollment> findByCourseId(Integer courseId);

    Optional<Enrollment> findByUserIdAndCourseId(Integer userId, Integer courseId);

    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);

    List<Enrollment> findByUserIdAndStatus(Integer userId, Enrollment.EnrollmentStatus status);

    long countByCourseIdAndStatus(Integer courseId, Enrollment.EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithCourses(@Param("userId") Integer userId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUsers(@Param("courseId") Integer courseId);
}
