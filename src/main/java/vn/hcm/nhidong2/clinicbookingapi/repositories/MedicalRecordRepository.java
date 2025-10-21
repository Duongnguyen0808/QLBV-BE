package vn.hcm.nhidong2.clinicbookingapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcm.nhidong2.clinicbookingapi.models.MedicalRecord;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // Tìm tất cả bệnh án của một bệnh nhân
    List<MedicalRecord> findByAppointment_Patient_IdOrderByAppointment_AppointmentDateTimeDesc(Long patientId);

    // Kiểm tra xem một lịch hẹn đã có bệnh án hay chưa
    boolean existsByAppointmentId(Long appointmentId);
}
