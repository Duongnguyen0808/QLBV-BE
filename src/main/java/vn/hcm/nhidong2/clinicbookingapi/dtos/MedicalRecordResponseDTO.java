package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.MedicalRecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Thông tin chi tiết của một bệnh án trong lịch sử khám")
public class MedicalRecordResponseDTO {
    private Long id;
    private OffsetDateTime appointmentDate;
    private String doctorName;
    private String specialtyName;
    private String diagnosis;
    private String symptoms;
    private String vitalSigns;
    private String testResults;
    private String prescription;
    private String notes;
    private LocalDate reexaminationDate; // THÊM TRƯỜNG NÀY

    public static MedicalRecordResponseDTO fromMedicalRecord(MedicalRecord record) {
        return MedicalRecordResponseDTO.builder()
                .id(record.getId())
                .appointmentDate(record.getAppointment().getAppointmentDateTime())
                .doctorName(record.getAppointment().getDoctor().getUser().getFullName())
                .specialtyName(record.getAppointment().getDoctor().getSpecialty().getName())
                .diagnosis(record.getDiagnosis())
                .symptoms(record.getSymptoms())
                .vitalSigns(record.getVitalSigns())
                .testResults(record.getTestResults())
                .prescription(record.getPrescription())
                .notes(record.getNotes())
                .reexaminationDate(record.getReexaminationDate()) // THÊM DÒNG NÀY
                .build();
    }
}