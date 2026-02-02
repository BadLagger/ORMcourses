package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Lesson;
import sf.mifi.grechko.models.Submission;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    @Query("SELECT s FROM Submission s JOIN FETCH s.assignment JOIN FETCH s.student")
    List<Submission> findAllWithDetails();
}
