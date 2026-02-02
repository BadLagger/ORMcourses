package sf.mifi.grechko.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "passing_score")
    private Integer passingScore; // Проходной балл в процентах

    @Column(name = "max_attempts")
    private Integer maxAttempts = 3; // Максимальное количество попыток

    @Column(name = "is_published")
    private Boolean isPublished = false;

    // Привязка к модулю (OneToOne)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", unique = false)
    private Module module;
}
