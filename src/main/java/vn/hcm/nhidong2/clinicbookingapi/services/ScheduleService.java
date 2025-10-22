package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.WorkingScheduleDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSession;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.WorkingScheduleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final WorkingScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    public List<WorkingSchedule> getSchedulesForDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    // Thêm một ca làm việc mới cho bác sĩ
    @Transactional
    public WorkingSchedule addSchedule(Long doctorId, WorkingScheduleDTO dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ."));
        
        // 1. Kiểm tra trùng lặp theo Ngày VÀ Ca
        scheduleRepository.findByDoctorIdAndDayOfWeekAndSession(
            doctorId, dto.getDayOfWeek(), dto.getSession()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Bác sĩ đã có lịch làm việc cho ngày " + dto.getDayOfWeek() + " ca " + dto.getSession() + " này.");
        });

        // 2. Lấy giờ chuẩn từ Enum
        WorkingSession session = dto.getSession();
        
        // SỬA LỖI: LƯU ĐÚNG THỨ TỰ start/end time. 
        // Giờ BẮT ĐẦU (nhỏ hơn) vào startTime, Giờ KẾT THÚC (lớn hơn) vào endTime.
        WorkingSchedule newSchedule = WorkingSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(dto.getDayOfWeek())
                .session(session)
                .startTime(session.getStartTime())   // Giờ Bắt đầu (đúng)
                .endTime(session.getEndTime())   // Giờ Kết thúc (đúng)
                .build();

        return scheduleRepository.save(newSchedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("Không tìm thấy lịch làm việc để xóa.");
        }
        scheduleRepository.deleteById(scheduleId);
    }
}