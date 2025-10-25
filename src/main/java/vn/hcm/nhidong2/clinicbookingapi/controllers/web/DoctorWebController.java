package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.MedicalRecordRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;
import vn.hcm.nhidong2.clinicbookingapi.services.AppointmentService;
import vn.hcm.nhidong2.clinicbookingapi.services.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;

import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/doctor/appointments") 
@RequiredArgsConstructor
public class DoctorWebController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService; 
    
    // Ánh xạ trạng thái sang tiếng Việt (SỬA 'Đã Hoàn Thành' thành 'Đã Khám')
    private static final Map<AppointmentStatus, String> STATUS_TRANSLATIONS = Map.of(
        AppointmentStatus.PENDING, "Chờ Xác Nhận",
        AppointmentStatus.PAID_PENDING, "Chờ Thanh Toán",
        AppointmentStatus.CONFIRMED, "Đã Xác Nhận",
        AppointmentStatus.COMPLETED, "Đã Khám", // <-- SỬA TÊN TRẠNG THÁI Ở ĐÂY
        AppointmentStatus.CANCELLED, "Đã Hủy"
    );
    
    // Định dạng giờ (Ví dụ: 07:30 - Th 5 23/10/2025)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm - E d/MM/yyyy");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Pattern MOBILE_UA = Pattern.compile("Android|iPhone|iPod|IEMobile|Windows Phone|BlackBerry|webOS|Opera Mini|Mobile", Pattern.CASE_INSENSITIVE);


    // SỬA LỖI: Định dạng ngày giờ và dịch trạng thái cho danh sách lịch hẹn
    @GetMapping
    public String showMyAppointmentsPage(Model model, HttpServletRequest request) {
        List<AppointmentResponseDTO> appointments = appointmentService.findAppointmentsForDoctor();
        
        // Chuyển đổi danh sách DTO sang Map để thêm các giá trị đã định dạng
        List<Map<String, Object>> formattedAppointments = appointments.stream()
            .map(appt -> {
                OffsetDateTime localDateTime = appt.getAppointmentDateTime()
                    .atZoneSameInstant(VIETNAM_ZONE)
                    .toOffsetDateTime();
                
                String formattedTime = localDateTime.format(DATETIME_FORMATTER);
                String translatedStatus = STATUS_TRANSLATIONS.getOrDefault(appt.getStatus(), appt.getStatus().name());
                
                Map<String, Object> map = new HashMap<>();
                map.put("patientFullName", appt.getPatient().getFullName());
                map.put("doctorName", appt.getDoctor().getFullName());
                map.put("specialtyName", appt.getDoctor().getSpecialtyName());
                map.put("formattedTime", formattedTime); 
                map.put("translatedStatus", translatedStatus); 
                map.put("status", appt.getStatus().name()); 
                map.put("id", appt.getId()); 
                return map;
            })
            .collect(Collectors.toList());

        String ua = request.getHeader("User-Agent");
        boolean isMobile = isMobileUserAgent(ua);

        model.addAttribute("appointments", formattedAppointments); 
        model.addAttribute("isMobile", isMobile);
        model.addAttribute("contentView", "doctor/appointments");
        return "fragments/layout";
    }

    private boolean isMobileUserAgent(String ua) {
        if (ua == null) return false;
        return MOBILE_UA.matcher(ua).find();
    }

    @GetMapping("/{id}/record/create")
    public String showCreateRecordPage(@PathVariable("id") Long appointmentId, Model model) {
        try {
            Appointment appointment = appointmentService.getAppointmentByIdForDoctor(appointmentId);
            model.addAttribute("appointment", appointment);
            model.addAttribute("medicalRecordRequest", new MedicalRecordRequestDTO());
            model.addAttribute("contentView", "doctor/record-create");
            return "fragments/layout";
        } catch (Exception e) {
            return "redirect:/doctor/appointments";
        }
    }

    @PostMapping("/{id}/record/create")
    public String processCreateRecord(
            @PathVariable("id") Long appointmentId,
            @ModelAttribute MedicalRecordRequestDTO medicalRecordRequest,
            RedirectAttributes ra) {
        try {
            medicalRecordRequest.setAppointmentId(appointmentId);
            medicalRecordService.createMedicalRecord(medicalRecordRequest);
            ra.addFlashAttribute("successMessage", "Tạo bệnh án thành công!");
        } catch (Exception e) {
            return "redirect:/doctor/appointments";
        }
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/{id}")
    public String showAppointmentDetailsPage(@PathVariable("id") Long appointmentId, Model model) {
        try {
            Appointment appointment = appointmentService.getAppointmentByIdForDoctor(appointmentId);
            
            OffsetDateTime localDateTime = appointment.getAppointmentDateTime().atZoneSameInstant(VIETNAM_ZONE).toOffsetDateTime();
            String formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
            
            String translatedStatus = STATUS_TRANSLATIONS.getOrDefault(appointment.getStatus(), appointment.getStatus().name());

            model.addAttribute("appointment", appointment); 
            model.addAttribute("formattedTime", formattedTime); 
            model.addAttribute("translatedStatus", translatedStatus); 
            model.addAttribute("contentView", "doctor/appointment-details");
            return "fragments/layout";
        } catch (Exception e) {
            return "redirect:/doctor/appointments";
        }
    }

    @PostMapping("/{id}/remind")
    public String remindAppointment(@PathVariable("id") Long appointmentId, RedirectAttributes ra) {
        try {
            appointmentService.sendReminder(appointmentId);
            ra.addFlashAttribute("successMessage", "Đã gửi nhắc lịch thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/{id}/confirm")
    public String confirmAppointment(@PathVariable("id") Long appointmentId, RedirectAttributes ra) {
        try {
            appointmentService.confirmAppointment(appointmentId);
            ra.addFlashAttribute("successMessage", "Xác nhận lịch hẹn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }
}