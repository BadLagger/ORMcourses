package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Enrollment;
import sf.mifi.grechko.models.Module;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {

    boolean existsByTitleAndCourseId(String title, Integer courseId);

    boolean existsByOrderIndex(Integer orderIndex);

    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) + 1 FROM Module m WHERE m.course.id = :courseId")
    Integer getNextOrderIndex(@Param("courseId") Integer courseId);

    @Query("SELECT m FROM Module m JOIN FETCH m.course WHERE m.id = :id")
    Optional<Module> findByIdWithDetails(Integer id);

    @Query("SELECT m FROM Module m JOIN FETCH m.course")
    List<Module> findAllWithDetails();
}
