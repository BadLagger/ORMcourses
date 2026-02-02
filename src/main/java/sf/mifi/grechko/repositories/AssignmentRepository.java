package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Assignment;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    boolean existsByTitleAndLessonId(String title, Integer lessonId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.lesson WHERE a.id = :id")
    Optional<Assignment> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.lesson")
    List<Assignment> findAllWithDetails();
}
