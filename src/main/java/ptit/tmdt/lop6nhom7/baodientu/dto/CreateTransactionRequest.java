package ptit.tmdt.lop6nhom7.baodientu.dto;
 
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
 
@Getter
@Setter
public class CreateTransactionRequest {
 
    @NotNull(message = "packageId không được để trống")
    private Integer packageId;
}