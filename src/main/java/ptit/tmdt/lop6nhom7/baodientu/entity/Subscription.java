package ptit.tmdt.lop6nhom7.baodientu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ptit.tmdt.lop6nhom7.baodientu.enums.SubscriptionTargetType;

@Getter
@Setter
@Entity
@Table(name = "subscriptions", schema = "pthttmdt")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @NotNull
  @Column(name = "target_id", nullable = false)
  private Integer targetId;
  
  @NotNull
  @Column(name = "target_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private SubscriptionTargetType targetType;
  
}