package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.DoctorReview;

import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Thông tin đánh giá bác sĩ")
public class ReviewResponseDTO {
    private Long id;
    private int rating;
    private String comment;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long appointmentId;
    private OffsetDateTime createdAt;

    public static ReviewResponseDTO fromEntity(DoctorReview r) {
        return ReviewResponseDTO.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .patientName(r.getPatient().getFullName())
                .doctorId(r.getDoctor().getId())
                .doctorName(r.getDoctor().getUser().getFullName())
                .appointmentId(r.getAppointment().getId())
                .createdAt(r.getCreatedAt())
                .build();
    }
}