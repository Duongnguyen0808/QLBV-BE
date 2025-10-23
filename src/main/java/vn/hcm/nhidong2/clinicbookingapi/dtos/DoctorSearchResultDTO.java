package vn.hcm.nhidong2.clinicbookingapi.dtos;

import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;

import java.util.List;

@Data
@Builder
public class DoctorSearchResultDTO {
    private Long doctorId;
    private String fullName;
    private String specialtyName;
    private Long specialtyId; // <--- DÒNG THÊM
    private List<WorkingScheduleInfoDTO> schedules;

    public static DoctorSearchResultDTO fromDoctor(Doctor doctor, List<WorkingScheduleInfoDTO> schedules) {
        return DoctorSearchResultDTO.builder()
                .doctorId(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .specialtyName(doctor.getSpecialty().getName())
                .specialtyId(doctor.getSpecialty().getId()) 
                .schedules(schedules)
                .build();
    }
}