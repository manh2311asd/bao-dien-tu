package ptit.tmdt.lop6nhom7.baodientu.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ptit.tmdt.lop6nhom7.baodientu.security.VNPayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPay integration service - handles payment URL generation and signature verification.
 * Based on VNPay 2.1.0 specification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    /**
     * Generate payment URL for VNPay gateway redirect.
     */
    public String generatePaymentUrl(PaymentRequest request, HttpServletRequest httpRequest) {
        log.debug("Generating VNPay payment URL for txnRef={}, amount={}", request.getTxnRef(), request.getAmount());

        requireGatewayConfiguration();

        long vnpayAmount = request.getAmount().movePointRight(2).longValueExact();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnpayAmount));
        vnp_Params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
        vnp_Params.put("vnp_TxnRef", request.getTxnRef());
        vnp_Params.put("vnp_OrderInfo", request.getOrderInfo());
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", vnPayConfig.getLocale());
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

        String ip = extractClientIp(httpRequest);
        vnp_Params.put("vnp_IpAddr", ip);

        TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(vietnamTimeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(vietnamTimeZone);

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            boolean firstParam = true;
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue == null || fieldValue.isEmpty()) {
                    continue;
                }
                if (!firstParam) {
                    query.append('&');
                    hashData.append('&');
                }
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8);
                hashData.append(fieldName).append('=').append(encodedValue);
                query.append(encodedName).append('=').append(encodedValue);
                firstParam = false;
            }
        } catch (Exception e) {
            throw new RuntimeException("URL encoding failed", e);
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = generateHmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        String paymentUrl = vnPayConfig.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("Payment URL generated - txnRef: {}, amount: {}", request.getTxnRef(), vnpayAmount);
        return paymentUrl;
    }
    
    /**
     * Verify VNPay callback signature from return URL or IPN.
     */
    public boolean verifyCallback(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            log.warn("Missing vnp_SecureHash in callback");
            return false;
        }
        
        Map<String, String> verifyParams = new TreeMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)) {
                verifyParams.put(key, value);
            }
        });
        
        StringBuilder hashData = new StringBuilder();
        List<String> keys = new ArrayList<>(verifyParams.keySet());
        Collections.sort(keys);
        
        boolean firstParam = true;
        for (String key : keys) {
            String value = verifyParams.get(key);
            if (value != null && !value.isEmpty()) {
                if (!firstParam) hashData.append("&");
                hashData.append(key).append("=").append(urlEncode(value));
                firstParam = false;
            }
        }
        
        String computedHash = generateHmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        boolean isValid = MessageDigest.isEqual(
                computedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                receivedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII)
        );
        
        log.debug("Signature verification - txnRef: {}, valid: {}", verifyParams.get("vnp_TxnRef"), isValid);
        return isValid;
    }
    
    /**
     * Generate HMAC-SHA512 signature.
     */
    private String generateHmacSHA512(String key, String data) {
        try {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"
        );
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
        }
        catch (Exception e) {
        throw new RuntimeException("HMAC-SHA512 failed", e);
        }
    }
    
    /**
     * URL encode callback values using UTF-8 as required by VNPay 2.1.0.
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error encoding value", e);
            throw new RuntimeException("Failed to encode value", e);
        }
    }

    private void requireGatewayConfiguration() {
        if (isBlank(vnPayConfig.getTmnCode())
                || isBlank(vnPayConfig.getHashSecret())
                || isBlank(vnPayConfig.getUrl())
                || isBlank(vnPayConfig.getReturnUrl())) {
            throw new IllegalStateException("VNPay is not configured on the server");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    
    /**
     * Extract client IP from HTTP request.
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        }
        
        String ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        
        return ip;
    }
    
    /**
     * Payment request DTO.
     */
    public static class PaymentRequest {
        private String txnRef;
        private java.math.BigDecimal amount;
        private String orderInfo;
        
        public PaymentRequest(String txnRef, java.math.BigDecimal amount, String orderInfo) {
            this.txnRef = txnRef;
            this.amount = amount;
            this.orderInfo = orderInfo;
        }
        
        public String getTxnRef() { return txnRef; }
        public java.math.BigDecimal getAmount() { return amount; }
        public String getOrderInfo() { return orderInfo; }
    }
}
