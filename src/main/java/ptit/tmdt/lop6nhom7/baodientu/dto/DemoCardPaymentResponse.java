package ptit.tmdt.lop6nhom7.baodientu.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import ptit.tmdt.lop6nhom7.baodientu.enums.TransactionStatus;

@Getter
@Builder
public class DemoCardPaymentResponse {
  private Integer transactionId;
  private String paymentCode;
  private BigDecimal amount;
  private TransactionStatus status;
  private Instant vipExpiryDate;
}
