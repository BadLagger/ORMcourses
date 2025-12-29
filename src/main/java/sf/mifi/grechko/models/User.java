package sf.mifi.grechko.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String passwdHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore  // Чтобы не было циклической ссылки в JSON
    private Profile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    public enum Role {
        ADMIN, USER, TEACHER
    }
}
