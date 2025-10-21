package vn.hcm.nhidong2.clinicbookingapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule, Long> {

    // Tìm tất cả các ca làm việc của một bác sĩ
    List<WorkingSchedule> findByDoctorId(Long doctorId);

    // Tìm một ca làm việc cụ thể của bác sĩ vào một ngày trong tuần
    Optional<WorkingSchedule> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}
