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
    // Thêm các trường rating tổng hợp
    private Double rating;
    private Integer reviewCount;
    private List<WorkingScheduleInfoDTO> schedules;

    public static DoctorSearchResultDTO fromDoctor(Doctor doctor, List<WorkingScheduleInfoDTO> schedules, Double rating, Integer reviewCount) {
        return DoctorSearchResultDTO.builder()
                .doctorId(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .specialtyName(doctor.getSpecialty().getName())
                .specialtyId(doctor.getSpecialty().getId()) 
                .rating(rating)
                .reviewCount(reviewCount)
                .schedules(schedules)
                .build();
    }
}