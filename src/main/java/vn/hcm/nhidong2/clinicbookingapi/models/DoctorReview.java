package vn.hcm.nhidong2.clinicbookingapi.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "doctor_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_appointment", columnNames = {"appointment_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}