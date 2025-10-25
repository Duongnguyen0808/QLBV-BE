package vn.hcm.nhidong2.clinicbookingapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.User;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);

    // Tìm các lịch hẹn của một bác sĩ trong một khoảng thời gian nhất định
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetweenAndStatusNot(
            Long doctorId, OffsetDateTime start, OffsetDateTime end, AppointmentStatus status
    );

    // Tìm tất cả lịch hẹn của một bác sĩ
    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeAsc(Long doctorId);
    // Tìm tất cả lịch hẹn trong một khoảng thời gian (dùng cho báo cáo theo ngày)
    List<Appointment> findAllByAppointmentDateTimeBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<User> findDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);
}
