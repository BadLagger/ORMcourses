package sf.mifi.grechko.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sf.mifi.grechko.models.Profile;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Integer userId);

    Optional<Profile> findByUserLogin(String login);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Profile> findByIdWithUser(@Param("id") Integer id);

    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.user.id = :userId")
    Optional<Profile> findByUserIdWithUser(@Param("userId") Integer userId);

    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.email = :email")
    Optional<Profile> findByEmail(String email);
}
