package ptit.tmdt.lop6nhom7.baodientu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response trả về cho VNPAY server sau khi nhận IPN.
 * VNPAY yêu cầu đúng format: {"RspCode":"00","Message":"Confirm Success"}
 * Nếu không nhận được response hợp lệ, VNPAY sẽ retry.
 */
@Getter
@AllArgsConstructor
public class VNPayIpnResponse {

    @JsonProperty("RspCode")
    private String rspCode;

    @JsonProperty("Message")
    private String message;

    // ── Factory methods theo từng trường hợp VNPAY quy định ──────────────────

    public static VNPayIpnResponse success() {
        return new VNPayIpnResponse("00", "Confirm Success");
    }

    public static VNPayIpnResponse invalidSignature() {
        return new VNPayIpnResponse("97", "Invalid Signature");
    }

    public static VNPayIpnResponse orderNotFound() {
        return new VNPayIpnResponse("01", "Order Not Found");
    }

    public static VNPayIpnResponse invalidAmount() {
        return new VNPayIpnResponse("04", "Invalid Amount");
    }

    public static VNPayIpnResponse alreadyConfirmed() {
        return new VNPayIpnResponse("02", "Order Already Confirmed");
    }
}