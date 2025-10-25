package vn.hcm.nhidong2.clinicbookingapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcm.nhidong2.clinicbookingapi.models.DoctorReview;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {
    Optional<DoctorReview> findByAppointment_Id(Long appointmentId);
    List<DoctorReview> findByDoctor_Id(Long doctorId);
    boolean existsByAppointment_Id(Long appointmentId);
}