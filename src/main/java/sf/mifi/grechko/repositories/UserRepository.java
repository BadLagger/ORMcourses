package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByRole(User.Role role);
    long countByRole(User.Role role);
}
