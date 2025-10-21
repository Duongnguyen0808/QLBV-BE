package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu để gán hoặc cập nhật chuyên khoa cho một bác sĩ")
public class AssignSpecialtyRequestDTO {
    @Schema(description = "ID của chuyên khoa muốn gán", example = "1")
    @NotNull(message = "ID chuyên khoa không được để trống")
    private Long specialtyId;
}
