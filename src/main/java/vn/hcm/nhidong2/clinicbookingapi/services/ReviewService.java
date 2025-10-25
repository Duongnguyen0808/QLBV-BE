package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReviewRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.*;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorReviewRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.MedicalRecordRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final AuthenticationService authenticationService;
    private final DoctorReviewRepository reviewRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public Optional<DoctorReview> getReviewByMedicalRecordId(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh án."));
        return reviewRepository.findByAppointment_Id(record.getAppointment().getId());
    }

    @Transactional
    public DoctorReview submitOrUpdateReview(Long recordId, ReviewRequestDTO request) {
        User currentPatient = authenticationService.getCurrentAuthenticatedUser();

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh án."));

        Appointment appointment = record.getAppointment();
        if (!appointment.getPatient().getId().equals(currentPatient.getId())) {
            throw new AccessDeniedException("Bạn chỉ có thể đánh giá các lịch hẹn của chính mình.");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ đánh giá sau khi lịch hẹn đã hoàn tất.");
        }

        Optional<DoctorReview> existing = reviewRepository.findByAppointment_Id(appointment.getId());
        DoctorReview review;
        if (existing.isPresent()) {
            review = existing.get();
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setUpdatedAt(OffsetDateTime.now());
        } else {
            review = DoctorReview.builder()
                    .appointment(appointment)
                    .doctor(appointment.getDoctor())
                    .patient(currentPatient)
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        return reviewRepository.save(review);
    }
}