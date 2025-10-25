package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReportDTO;
import vn.hcm.nhidong2.clinicbookingapi.services.ReportService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportWebController {

    private final ReportService reportService;

    @GetMapping
    public String showReportsPage(
            Model model,
            @RequestParam(name = "selectedDate", required = false) Optional<String> selectedDateStr
    ) {
        
        // --- Xử lý Báo cáo theo Ngày ---
        LocalDate selectedDate;
        if (selectedDateStr.isPresent() && !selectedDateStr.get().isBlank()) {
            try {
                selectedDate = LocalDate.parse(selectedDateStr.get());
            } catch (Exception e) {
                selectedDate = LocalDate.now();
            }
        } else {
            selectedDate = LocalDate.now();
        }
        
        List<ReportDTO> dailyReport = reportService.getDailyAppointmentReport(selectedDate);
        model.addAttribute("dailyReport", dailyReport);
        model.addAttribute("selectedDate", selectedDate.toString());


        // --- Lấy các báo cáo tổng quan khác ---
        
        // Báo cáo theo Trạng thái Lịch hẹn (Tổng quan)
        List<ReportDTO> statusReport = reportService.getAppointmentStatusReport();
        model.addAttribute("statusReport", statusReport);

        // Báo cáo Tải trọng Bác sĩ
        List<ReportDTO> doctorLoadReport = reportService.getDoctorLoadReport();
        model.addAttribute("doctorLoadReport", doctorLoadReport);
        
        // Báo cáo Tải trọng Chuyên khoa
        List<ReportDTO> specialtyLoadReport = reportService.getAppointmentsBySpecialtyReport();
        model.addAttribute("specialtyLoadReport", specialtyLoadReport);


        model.addAttribute("contentView", "admin/reports");
        return "fragments/layout";
    }
}
