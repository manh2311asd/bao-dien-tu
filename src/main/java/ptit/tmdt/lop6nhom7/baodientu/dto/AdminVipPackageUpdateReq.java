package ptit.tmdt.lop6nhom7.baodientu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminVipPackageUpdateReq {
  @NotBlank(message = "Ten goi khong duoc de trong")
  private String name;

  @NotNull(message = "Thoi han khong duoc de trong")
  @Positive(message = "Thoi han phai la so duong")
  private Integer durationDays;

  @NotNull(message = "Gia goi khong duoc de trong")
  @DecimalMin(value = "0.0", inclusive = false, message = "Gia goi phai lon hon 0")
  private BigDecimal price;

  @NotNull(message = "Khuyen mai khong duoc de trong")
  @Min(value = 0, message = "Khuyen mai toi thieu la 0")
  @Max(value = 100, message = "Khuyen mai toi da la 100")
  private Integer discountPercent;

  private String description;
}
