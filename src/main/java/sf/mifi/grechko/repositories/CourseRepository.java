package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Course;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByTeacherId(Integer teacherId);

    List<Course> findByCategoryId(Integer categoryId);

    List<Course> findByStartDateAfter(LocalDate date);

    List<Course> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT c FROM Course c JOIN FETCH c.teacher JOIN FETCH c.category WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT c FROM Course c JOIN FETCH c.teacher JOIN FETCH c.category")
    List<Course> findAllWithDetails();

    boolean existsByTitleAndTeacherId(String title, Integer teacherId);
}
