package ptit.tmdt.lop6nhom7.baodientu.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ptit.tmdt.lop6nhom7.baodientu.dto.CreateTransactionRequest;
import ptit.tmdt.lop6nhom7.baodientu.dto.TransactionResponse;
import ptit.tmdt.lop6nhom7.baodientu.dto.VNPayIpnResponse;
import ptit.tmdt.lop6nhom7.baodientu.dto.VNPayPaymentResponse;
import ptit.tmdt.lop6nhom7.baodientu.service.TransactionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transaction Controller - Handles payment flow endpoints.
 * 
 * API Endpoints:
 * - POST /api/transactions/create        (protected) - Initiate payment
 * - GET  /api/transactions/vnpay-return  (public)    - VNPay return callback
 * - GET  /api/transactions/vnpay-ipn     (public)    - VNPay IPN notification
 * - GET  /api/transactions/my            (protected) - Transaction history
 * 
 * Payment Flow:
 * 1. User calls /create with packageId
 * 2. Backend creates transaction, returns VNPay URL
 * 3. Frontend redirects user to VNPay gateway
 * 4. User completes payment on VNPay
 * 5. VNPay redirects to /vnpay-return (user sees result)
 * 6. VNPay calls /vnpay-ipn (backend processes payment)
 * 7. If successful, activate user VIP subscription
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions/create
     * 
     * Initiate payment for VIP package purchase.
     * 
     * Request:
     * - packageId: VIP package ID to purchase
     * 
     * Response:
     * - paymentUrl: URL to redirect user to VNPay gateway
     * 
     * Requires JWT authentication.
     */
    @PostMapping("/create")
    public ResponseEntity<VNPayPaymentResponse> createPayment(
            @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Payment creation requested for packageId={}", request.getPackageId());
        
        String paymentUrl = transactionService.initiatePayment(
                request.getPackageId(), 
                httpRequest
        );
        
        return ResponseEntity.ok(new VNPayPaymentResponse(paymentUrl));
    }

    /**
     * GET /api/transactions/vnpay-return
     * 
     * VNPay redirect callback - called when user completes/cancels payment.
     * 
     * This endpoint ONLY displays payment result to user.
     * All database updates happen in vnpay-ipn endpoint only.
     * 
     * Query Parameters: VNPay response parameters (vnp_Amount, vnp_BankCode, etc.)
     * 
     * Response:
     * - "Thanh toán thành công!" if payment succeeded
     * - "Thanh toán thất bại" if payment failed
     * 
     * Public endpoint - no authentication required.
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(HttpServletRequest request) {
        
        log.debug("VNPay return callback received");
        
        // Extract VNPay response parameters
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            params.put(key, value[0]);
        });
        
        String txnRef = params.get("vnp_TxnRef");
        log.info("Return callback for txnRef={}", txnRef);
        
        // Check if payment successful
        boolean success = transactionService.getPaymentResult(params);
        
        if (success) {
            log.info("Displaying success message for txnRef={}", txnRef);
            return ResponseEntity.ok("Thanh toán thành công!");
        } else {
            log.info("Displaying failure message for txnRef={}", txnRef);
            return ResponseEntity.badRequest().body("Thanh toán thất bại");
        }
    }

    /**
     * GET /api/transactions/vnpay-ipn
     * 
     * VNPay IPN notification callback - called by VNPay server to notify payment result.
     * 
     * This is the AUTHORITATIVE payment result notification.
     * This endpoint is responsible for:
     * - Verifying payment signature
     * - Updating transaction status
     * - Activating user VIP subscription
     * 
     * VNPay will retry automatically if response code is not "00".
     * Response must be in exact format: {"RspCode":"00","Message":"..."}
     * 
     * Query Parameters: VNPay IPN parameters
     * 
     * Response Format (required by VNPay):
     * {
     *   "RspCode": "00" (success) or error code,
     *   "Message": "Confirm Success" or error message
     * }
     * 
     * Public endpoint - no authentication required (VNPay server calls this).
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<VNPayIpnResponse> vnpayIpn(HttpServletRequest request) {
        
        log.debug("VNPay IPN notification received");
        
        // Extract VNPay IPN parameters
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            params.put(key, value[0]);
        });
        
        String txnRef = params.get("vnp_TxnRef");
        log.info("IPN for txnRef={}", txnRef);
        
        // Process IPN and update transaction
        VNPayIpnResponse response = transactionService.handleIpn(params);
        
        log.info("IPN response: code={}, message={}", response.getRspCode(), response.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/transactions/my
     * 
     * Get transaction history for current authenticated user.
     * 
     * Response:
     * - List of transactions with details:
     *   - id
     *   - packageName
     *   - amount
     *   - status (PENDING, SUCCESS, FAILED, CANCELED)
     *   - paymentCode
     *   - createdAt
     * 
     * Requires JWT authentication.
     */
    @GetMapping("/my")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        
        log.debug("Fetching transaction history");
        List<TransactionResponse> transactions = transactionService.getMyTransactions();
        
        return ResponseEntity.ok(transactions);
    }
}