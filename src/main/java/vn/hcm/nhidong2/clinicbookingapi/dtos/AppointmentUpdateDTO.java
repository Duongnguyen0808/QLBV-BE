package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "Yêu cầu để cập nhật thời gian của một lịch hẹn")
public class AppointmentUpdateDTO {

    @Schema(description = "Ngày và giờ mới (phải là một thời điểm trong tương lai)",
            example = "2025-10-21T14:30:00+07:00")
    @NotNull(message = "Thời gian hẹn mới không được để trống")
    @Future(message = "Thời gian hẹn mới phải là một thời điểm trong tương lai")
    private OffsetDateTime newAppointmentDateTime;
}
