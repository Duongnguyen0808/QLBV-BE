package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Đối tượng dữ liệu cho Chuyên khoa")
public class SpecialtyDTO {
    @Schema(description = "ID của chuyên khoa", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Tên chuyên khoa", example = "Khoa Tim mạch")
    @NotBlank(message = "Tên chuyên khoa không được để trống")
    private String name;

    public static SpecialtyDTO fromSpecialty(Specialty specialty) {
        return new SpecialtyDTO(specialty.getId(), specialty.getName());
    }
}
