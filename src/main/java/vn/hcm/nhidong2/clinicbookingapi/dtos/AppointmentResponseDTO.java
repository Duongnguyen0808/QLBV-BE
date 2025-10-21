package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;

import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Thông tin chi tiết của một lịch hẹn")
public class AppointmentResponseDTO {

    @Schema(description = "ID của lịch hẹn", example = "101")
    private Long id;

    @Schema(description = "Thông tin bệnh nhân")
    private UserResponseDTO patient;

    @Schema(description = "Thông tin bác sĩ")
    private DoctorResponseDTO doctor;

    @Schema(description = "Thời gian hẹn", example = "2025-10-20T10:00:00+07:00")
    private OffsetDateTime appointmentDateTime;

    @Schema(description = "Thời lượng của buổi khám (tính bằng phút). Nếu để trống, mặc định là 30.", example = "45")
    private Integer duration;

    @Schema(description = "Ghi chú của bệnh nhân", example = "Bé bị ho và sốt nhẹ")
    private String notes;

    @Schema(description = "Trạng thái của lịch hẹn", example = "PENDING")
    private AppointmentStatus status;

    @Schema(description = "ID của bệnh án liên quan (nếu có)", example = "55")
    private Long medicalRecordId;

    public static AppointmentResponseDTO fromAppointment(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patient(UserResponseDTO.fromUser(appointment.getPatient()))
                .doctor(DoctorResponseDTO.fromDoctor(appointment.getDoctor())) // Cần tạo DTO này
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .duration(appointment.getDuration())
                .notes(appointment.getNotes())
                .status(appointment.getStatus())
                .medicalRecordId(appointment.getMedicalRecord() != null ? appointment.getMedicalRecord().getId() : null)
                .build();
    }
}
