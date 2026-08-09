package ptit.tmdt.lop6nhom7.baodientu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
    name = "comments",
    schema = "pthttmdt",
    indexes = {
        @Index(name = "idx_comments_article_created", columnList = "article_id, created_at"),
        @Index(name = "idx_comments_user_created", columnList = "user_id, created_at")
    }
)
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @NotNull
  @Column(name = "content", nullable = false)
  private String content;
  
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at")
  private Instant createdAt;
  
}