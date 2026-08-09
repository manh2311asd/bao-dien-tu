package ptit.tmdt.lop6nhom7.baodientu.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ptit.tmdt.lop6nhom7.baodientu.security.VNPayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Iterator;

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

        long vnpayAmount = request.getAmount().longValue() * 100;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnpayAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", request.getTxnRef());
        vnp_Params.put("vnp_OrderInfo", request.getOrderInfo());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

        String ip = extractClientIp(httpRequest);
        vnp_Params.put("vnp_IpAddr", ip);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

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
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);

                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("URL encoding failed", e);
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = generateHmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        String paymentUrl = vnPayConfig.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("Payment URL generated - txnRef: {}, amount: {}", request.getTxnRef(), vnpayAmount);
        log.debug("Payment hash data: {}", hashData);

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
        
        Map<String, String> verifyParams = new TreeMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");
        
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
        boolean isValid = computedHash.equalsIgnoreCase(receivedHash);
        
        log.debug("Signature verification - txnRef: {}, valid: {}", verifyParams.get("vnp_TxnRef"), isValid);
        if (!isValid) {
            log.warn("Invalid signature - received: {}, computed: {}", receivedHash, computedHash);
        }
        
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
     * URL encode value using US_ASCII charset (VNPay standard).
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.US_ASCII.name());
        } catch (Exception e) {
            log.error("Error encoding value", e);
            throw new RuntimeException("Failed to encode value", e);
        }
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