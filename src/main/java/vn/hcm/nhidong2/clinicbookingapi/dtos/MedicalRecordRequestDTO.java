package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Yêu cầu để tạo một bệnh án mới sau khi khám")
public class MedicalRecordRequestDTO {

    @Schema(description = "ID của lịch hẹn đã hoàn thành", example = "101")
    @NotNull(message = "ID lịch hẹn không được để trống")
    private Long appointmentId;

    @Schema(description = "Chẩn đoán của bác sĩ", example = "Viêm họng cấp")
    @NotBlank(message = "Chẩn đoán không được để trống")
    private String diagnosis;

    @Schema(description = "Các triệu chứng của bệnh nhân", example = "Sốt cao, ho, đau họng")
    private String symptoms;

    @Schema(description = "Các dấu hiệu sinh tồn", example = "Nhiệt độ: 38.5°C, Mạch: 90 lần/phút")
    private String vitalSigns;

    @Schema(description = "Kết quả xét nghiệm tóm tắt", example = "Xét nghiệm máu: Bạch cầu tăng nhẹ")
    private String testResults;

    @Schema(description = "Đơn thuốc", example = "Amoxicillin 500mg, Paracetamol 500mg")
    private String prescription;

    @Schema(description = "Ghi chú thêm của bác sĩ", example = "Tái khám sau 5 ngày nếu không đỡ")
    private String notes;
    
    @Schema(description = "Ngày gợi ý tái khám", example = "2025-11-01") // THÊM TRƯỜNG NÀY
    private LocalDate reexaminationDate;
}