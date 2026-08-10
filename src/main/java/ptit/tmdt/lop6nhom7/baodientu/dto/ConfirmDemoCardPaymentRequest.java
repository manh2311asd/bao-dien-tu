package ptit.tmdt.lop6nhom7.baodientu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmDemoCardPaymentRequest {

  @NotNull(message = "transactionId không được để trống")
  private Integer transactionId;

  @NotBlank(message = "paymentCode không được để trống")
  private String paymentCode;
}
