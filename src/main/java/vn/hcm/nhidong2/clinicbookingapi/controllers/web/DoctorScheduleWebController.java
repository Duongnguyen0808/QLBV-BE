package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.dtos.WorkingScheduleDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.services.AuthenticationService;
import vn.hcm.nhidong2.clinicbookingapi.services.ScheduleService;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctor/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_DOCTOR')") // Chỉ cho phép vai trò DOCTOR truy cập
public class DoctorScheduleWebController {

    private final ScheduleService scheduleService;
    private final AuthenticationService authenticationService;
    private final DoctorRepository doctorRepository;

    private static final Map<DayOfWeek, String> DAY_OF_WEEK_TRANSLATIONS = new LinkedHashMap<>();
    static {
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.MONDAY, "Thứ Hai");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.TUESDAY, "Thứ Ba");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.WEDNESDAY, "Thứ Tư");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.THURSDAY, "Thứ Năm");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.FRIDAY, "Thứ Sáu");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.SATURDAY, "Thứ Bảy");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.SUNDAY, "Chủ Nhật");
    }

    private Doctor getCurrentDoctor() {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        return doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new AccessDeniedException("Hồ sơ bác sĩ không tồn tại."));
    }

    // Hiển thị trang quản lý lịch
    @GetMapping
    public String showSchedulePage(Model model) {
        Doctor doctor = getCurrentDoctor();
        List<WorkingSchedule> schedules = scheduleService.getSchedulesForDoctor(doctor.getId());
        schedules.sort(Comparator.comparing(s -> s.getDayOfWeek().getValue()));

        model.addAttribute("doctor", doctor);
        model.addAttribute("schedules", schedules);
        model.addAttribute("newSchedule", new WorkingScheduleDTO());
        model.addAttribute("dayOfWeekTranslations", DAY_OF_WEEK_TRANSLATIONS);
        model.addAttribute("contentView", "doctor/doctor-schedule"); // <-- Dùng view mới
        return "fragments/layout";
    }

    // Xử lý thêm lịch làm việc mới
    @PostMapping("/add")
    public String addSchedule(@ModelAttribute("newSchedule") WorkingScheduleDTO dto,
                              RedirectAttributes ra) {
        Long doctorId = getCurrentDoctor().getId();
        try {
            scheduleService.addSchedule(doctorId, dto);
            ra.addFlashAttribute("successMessage", "Thêm lịch làm việc thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/doctor/schedules";
    }

    // Xử lý xóa lịch làm việc
    @PostMapping("/{scheduleId}/delete")
    public String deleteSchedule(@PathVariable Long scheduleId, RedirectAttributes ra) {
        Long doctorId = getCurrentDoctor().getId();
        try {
            scheduleService.deleteSchedule(scheduleId);
            ra.addFlashAttribute("successMessage", "Xóa lịch làm việc thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/doctor/schedules";
    }
}