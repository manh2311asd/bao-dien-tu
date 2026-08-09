package ptit.tmdt.lop6nhom7.baodientu.dto;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
 
@Getter
@AllArgsConstructor
public class VNPayPaymentResponse {
 
    /** URL để frontend redirect sang cổng thanh toán VNPAY */
    private String paymentUrl;
}