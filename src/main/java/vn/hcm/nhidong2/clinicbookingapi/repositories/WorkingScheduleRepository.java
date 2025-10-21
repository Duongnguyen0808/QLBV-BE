package vn.hcm.nhidong2.clinicbookingapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSession;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule, Long> {

    List<WorkingSchedule> findByDoctorId(Long doctorId);

    // Phương thức kiểm tra trùng lặp theo Ngày VÀ Ca
    Optional<WorkingSchedule> findByDoctorIdAndDayOfWeekAndSession(
        Long doctorId, DayOfWeek dayOfWeek, WorkingSession session
    );
    
    // Phương thức cũ (nếu có) đã bị loại bỏ/sửa theo WorkingSession.
}