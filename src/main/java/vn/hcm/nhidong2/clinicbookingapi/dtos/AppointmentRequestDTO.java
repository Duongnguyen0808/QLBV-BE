package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "Yêu cầu để tạo một lịch hẹn mới")
public class AppointmentRequestDTO {

    @Schema(description = "ID của chuyên khoa muốn đặt lịch", example = "1")
    @NotNull(message = "ID chuyên khoa không được để trống")
    private Long specialtyId;

    @Schema(description = "ID của bác sĩ muốn đặt lịch (tùy chọn). Nếu để trống, hệ thống sẽ tự động chọn bác sĩ phù hợp.", example = "1")
    private Long doctorId;

    @Schema(description = "Ngày và giờ muốn đặt lịch (phải là một thời điểm trong tương lai)",
            example = "2025-10-20T10:00:00+07:00")
    @NotNull(message = "Thời gian hẹn không được để trống")
    @Future(message = "Thời gian hẹn phải là một thời điểm trong tương lai")
    private OffsetDateTime appointmentDateTime;

    @Schema(description = "Thời lượng của buổi khám (tính bằng phút). Nếu để trống, mặc định là 30.", example = "45")
    private Integer duration;

    @Schema(description = "Ghi chú của bệnh nhân cho bác sĩ", example = "Bé bị ho và sốt nhẹ")
    private String notes;
}
