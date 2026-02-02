package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Quiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    boolean existsByTitleAndModuleId(String title, Integer moduleId);

    @Query("SELECT q FROM Quiz q JOIN FETCH q.module WHERE q.id = :id")
    Optional<Quiz> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT q FROM Quiz q JOIN FETCH q.module")
    List<Quiz> findAllWithDetails();
}
