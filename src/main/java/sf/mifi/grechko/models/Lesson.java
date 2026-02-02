package sf.mifi.grechko.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    /*@OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    @Builder.Default
    @JsonIgnore
    private List<Assignment> assignments = new ArrayList<>();*/

}
