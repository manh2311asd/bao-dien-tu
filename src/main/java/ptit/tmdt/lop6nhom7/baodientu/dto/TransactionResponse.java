package ptit.tmdt.lop6nhom7.baodientu.dto;
 
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ptit.tmdt.lop6nhom7.baodientu.enums.TransactionStatus;
 
import java.math.BigDecimal;
import java.time.Instant;
 
@Getter
@Setter
@Builder
public class TransactionResponse {
 
    private Integer           id;
    private String            packageName;
    private BigDecimal        amount;
    private TransactionStatus status;
    private String            paymentCode;   // vnp_TxnRef
    private Instant           createdAt;
}