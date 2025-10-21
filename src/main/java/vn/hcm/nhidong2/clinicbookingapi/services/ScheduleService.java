package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.WorkingScheduleDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.WorkingScheduleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final WorkingScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    // Lấy tất cả lịch làm việc của một bác sĩ
    public List<WorkingSchedule> getSchedulesForDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    // Thêm một ca làm việc mới cho bác sĩ
    @Transactional
    public WorkingSchedule addSchedule(Long doctorId, WorkingScheduleDTO dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ."));

        if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime())) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        }

        scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dto.getDayOfWeek())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Bác sĩ đã có lịch làm việc cho ngày này.");
                });

        WorkingSchedule newSchedule = WorkingSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();

        return scheduleRepository.save(newSchedule);
    }

    // Xóa một ca làm việc
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("Không tìm thấy lịch làm việc để xóa.");
        }
        scheduleRepository.deleteById(scheduleId);
    }
}