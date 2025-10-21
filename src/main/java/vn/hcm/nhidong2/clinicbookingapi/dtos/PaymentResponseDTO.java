package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Đối tượng trả về chứa thông tin/link để chuyển hướng thanh toán")
public class PaymentResponseDTO {
    @Schema(description = "URL để chuyển hướng người dùng đến cổng thanh toán", example = "https://sandbox.vnpayment.vn/paymentv2/payment-web...")
    private String paymentUrl;

    @Schema(description = "Mã giao dịch nội bộ", example = "1")
    private Long transactionId;

    @Schema(description = "Tổng số tiền cần thanh toán", example = "150000")
    private Long amount;
}