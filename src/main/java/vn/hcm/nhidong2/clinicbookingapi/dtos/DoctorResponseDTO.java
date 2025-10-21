package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;

@Data
@Builder
@Schema(description = "Thông tin công khai của một bác sĩ")
public class DoctorResponseDTO {

    @Schema(description = "ID của bác sĩ", example = "1")
    private Long doctorId;

    @Schema(description = "Tên đầy đủ của bác sĩ", example = "Bác sĩ Chuyên khoa II Trần Thị A")
    private String fullName;

    @Schema(description = "Tên chuyên khoa", example = "Khoa Hô hấp")
    private String specialtyName;

    public static DoctorResponseDTO fromDoctor(Doctor doctor) {
        return DoctorResponseDTO.builder()
                .doctorId(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .specialtyName(doctor.getSpecialty().getName())
                .build();
    }
}
