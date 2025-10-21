package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; // THÊM DÒNG NÀY
import lombok.Data;
import lombok.NoArgsConstructor; // THÊM DÒNG NÀY

@Data
@AllArgsConstructor // THÊM DÒNG NÀY
@NoArgsConstructor // THÊM DÒNG NÀY (để đảm bảo khả năng deserialization từ JSON)
@Schema(description = "Yêu cầu khởi tạo thanh toán cho một lịch hẹn")
public class PaymentRequestDTO {

    @Schema(description = "ID của lịch hẹn cần thanh toán", example = "101")
    @NotNull(message = "ID lịch hẹn không được để trống")
    private Long appointmentId;

    @Schema(description = "Mã phương thức thanh toán (ví dụ: VNPAY, MOMO)", example = "VNPAY")
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;
}