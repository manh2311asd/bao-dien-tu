package ptit.tmdt.lop6nhom7.baodientu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ptit.tmdt.lop6nhom7.baodientu.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transactions", schema = "pthttmdt")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "package_id", nullable = false)
  private VipPackage packageField;
  
  @NotNull
  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;
  
  @Size(max = 255)
  @Column(name = "payment_code")
  private String paymentCode;
  
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private TransactionStatus status;
  
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at")
  private Instant createdAt;
  
}