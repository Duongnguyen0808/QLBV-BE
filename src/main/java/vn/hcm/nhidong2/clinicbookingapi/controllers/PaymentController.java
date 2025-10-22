package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PaymentRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PaymentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.PaymentStatus;
import vn.hcm.nhidong2.clinicbookingapi.services.PaymentService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "Các API để xử lý thanh toán trực tuyến")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Khởi tạo yêu cầu thanh toán",
            description = "Bệnh nhân khởi tạo yêu cầu thanh toán cho một lịch hẹn đang chờ thanh toán. API trả về URL (QR code) để mobile app/web hiển thị.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Khởi tạo thành công, trả về URL thanh toán"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc lịch hẹn không đủ điều kiện thanh toán"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thanh toán cho lịch hẹn này")
    })
    @PostMapping("/create-request")
    public ResponseEntity<?> createPaymentRequest(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        try {
            PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | AccessDeniedException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint giả lập để cổng thanh toán gọi lại sau khi người dùng hoàn tất giao dịch
    @Operation(summary = "Xử lý Callback/IPN từ Cổng thanh toán (Mô phỏng)",
            description = "Endpoint này được cổng thanh toán gọi đến. Bệnh nhân không gọi trực tiếp.")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Xử lý thành công"))
    @GetMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(
            @RequestParam("txnId") Long transactionId,
            @RequestParam("status") String status, // SUCCESS or FAILED
            @RequestParam(value = "transactionCode", required = false) String transactionCode
    ) {
        // KHÔNG CẦN TỰ TẠO UUID NẾU LÀ NULL NỮA. SERVICE SẼ XỬ LÝ.
        try {
            String result = paymentService.handlePaymentCallback(transactionId, status, transactionCode);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý callback: " + e.getMessage());
        }
    }

    @Operation(summary = "Kiểm tra trạng thái giao dịch",
            description = "Dùng cho Web/Mobile App để kiểm tra liên tục trạng thái thanh toán.")
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<Map<String, String>> getTransactionStatus(@PathVariable Long transactionId) {
        try {
            PaymentStatus status = paymentService.getTransactionStatus(transactionId);
            return ResponseEntity.ok(Map.of("status", status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}