package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu để tạo một hồ sơ Bác sĩ và gán chuyên khoa cho một User")
public class DoctorCreationRequestDTO {
    @Schema(description = "ID của tài khoản User sẽ được gán làm bác sĩ", example = "5")
    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @Schema(description = "ID của chuyên khoa được gán cho bác sĩ", example = "1")
    @NotNull(message = "Specialty ID không được để trống")
    private Long specialtyId;
}
