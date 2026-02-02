package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Lesson;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {

    boolean existsByTitleAndModuleId(String title, Integer moduleId);

    @Query("SELECT l FROM Lesson l JOIN FETCH l.module WHERE l.id = :id")
    Optional<Lesson> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT l FROM Lesson l JOIN FETCH l.module")
    List<Lesson> findAllWithDetails();
}
