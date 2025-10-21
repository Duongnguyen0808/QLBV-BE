package vn.hcm.nhidong2.clinicbookingapi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "medical_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mỗi bệnh án thuộc về duy nhất một lịch hẹn
    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "diagnosis", columnDefinition = "TEXT", nullable = false)
    private String diagnosis; // Chẩn đoán

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms; // Triệu chứng

    @Column(name = "vital_signs", columnDefinition = "TEXT")
    private String vitalSigns; // Dấu hiệu sinh tồn

    @Column(name = "test_results", columnDefinition = "TEXT")
    private String testResults; // Kết quả xét nghiệm

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription; // Đơn thuốc

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Ghi chú thêm của bác sĩ

    @Column(name = "reexamination_date") // THÊM DÒNG NÀY: Ngày tái khám
    private LocalDate reexaminationDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}