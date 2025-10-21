package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.MedicalRecordRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.MedicalRecordResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.MedicalRecord;
import vn.hcm.nhidong2.clinicbookingapi.services.AppointmentService;
import vn.hcm.nhidong2.clinicbookingapi.services.MedicalRecordService;

import java.util.List;

@Controller
@RequestMapping("/doctor/appointments") // Đặt đường dẫn gốc cho quản lý lịch hẹn của bác sĩ
@RequiredArgsConstructor
public class DoctorWebController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService; // Thêm service mới

    // Hiển thị trang danh sách lịch hẹn
    @GetMapping
    public String showMyAppointmentsPage(Model model) {
        List<AppointmentResponseDTO> appointments = appointmentService.findAppointmentsForDoctor();
        model.addAttribute("appointments", appointments);
        model.addAttribute("contentView", "doctor/appointments");
        return "fragments/layout";
    }

    // THÊM MỚI: Hiển thị form tạo bệnh án
    @GetMapping("/{id}/record/create")
    public String showCreateRecordPage(@PathVariable("id") Long appointmentId, Model model) {
        try {
            Appointment appointment = appointmentService.getAppointmentByIdForDoctor(appointmentId);
            model.addAttribute("appointment", appointment);
            // Tạo một DTO rỗng để Thymeleaf binding vào form
            model.addAttribute("medicalRecordRequest", new MedicalRecordRequestDTO());
            model.addAttribute("contentView", "doctor/record-create");
            return "fragments/layout";
        } catch (Exception e) {
            // Xử lý nếu bác sĩ cố truy cập lịch hẹn không phải của mình
            return "redirect:/doctor/appointments";
        }
    }

    // THÊM MỚI: Xử lý việc tạo bệnh án từ form
    @PostMapping("/{id}/record/create")
    public String processCreateRecord(
            @PathVariable("id") Long appointmentId,
            @ModelAttribute MedicalRecordRequestDTO medicalRecordRequest,
            RedirectAttributes ra) {
        try {
            // Gán ID lịch hẹn từ URL vào DTO trước khi gửi cho service
            medicalRecordRequest.setAppointmentId(appointmentId);
            medicalRecordService.createMedicalRecord(medicalRecordRequest);
            ra.addFlashAttribute("successMessage", "Tạo bệnh án thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/{id}")
    public String showAppointmentDetailsPage(@PathVariable("id") Long appointmentId, Model model) {
        try {
            Appointment appointment = appointmentService.getAppointmentByIdForDoctor(appointmentId);
            model.addAttribute("appointment", appointment); // Truyền cả object Appointment
            model.addAttribute("contentView", "doctor/appointment-details");
            return "fragments/layout";
        } catch (Exception e) {
            return "redirect:/doctor/appointments";
        }
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