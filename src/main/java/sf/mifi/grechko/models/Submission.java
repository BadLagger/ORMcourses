package sf.mifi.grechko.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "score")
    private Integer score; // Оценка (null пока не проверено)

    @Column(columnDefinition = "TEXT")
    private String feedback; // Комментарий преподавателя

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.SUBMITTED;

    public enum Status {
        SUBMITTED,      // Отправлено, но не проверено
        UNDER_REVIEW,   // На проверке
        GRADED,         // Проверено с оценкой
        NEEDS_REVISION, // Требует доработки
        LATE,           // Сдано после дедлайна
        REJECTED        // Отклонено
    }
}
