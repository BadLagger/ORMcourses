package sf.mifi.grechko.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enroll_date", nullable = false)
    private LocalDate enrollDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EnrollmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    // Дополнительные методы для удобства
    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return this.status == EnrollmentStatus.COMPLETED;
    }

    @PrePersist
    protected void onCreate() {
        if (enrollDate == null) {
            enrollDate = LocalDate.now();
        }
        if (status == null) {
            status = EnrollmentStatus.ACTIVE;
        }
    }
}