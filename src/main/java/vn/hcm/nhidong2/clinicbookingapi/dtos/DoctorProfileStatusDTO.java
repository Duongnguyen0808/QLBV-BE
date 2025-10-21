package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Trạng thái hồ sơ của bác sĩ, cho biết đã gán chuyên khoa hay chưa")
public class DoctorProfileStatusDTO {
    @Schema(description = "True nếu bác sĩ đã có hồ sơ và chuyên khoa, ngược lại là False", example = "true")
    private boolean hasProfile;

    @Schema(description = "Thông tin chi tiết hồ sơ bác sĩ (chỉ có khi hasProfile = true)")
    private DoctorResponseDTO doctorProfile;
}
