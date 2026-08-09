package ptit.tmdt.lop6nhom7.baodientu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class VipPackageRes {
  private Integer id;
  private String name;
  private Integer durationDays;
  private BigDecimal price;
  private Integer discountPercent;
  private String description;
}
