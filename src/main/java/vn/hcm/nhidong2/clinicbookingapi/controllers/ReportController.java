package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; // THÊM
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; // THÊM
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // THÊM
import org.springframework.web.bind.annotation.RestController;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReportDTO;
import vn.hcm.nhidong2.clinicbookingapi.services.ReportService;

import java.time.LocalDate; // THÊM
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "Các API thống kê và báo cáo (yêu cầu quyền ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Thống kê số lượng lịch hẹn theo trạng thái",
            description = "Lấy báo cáo tổng quan về số lượng lịch hẹn ở các trạng thái (PENDING, CONFIRMED, COMPLETED, CANCELLED, PAID_PENDING).")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/appointment-status")
    public ResponseEntity<List<ReportDTO>> getAppointmentStatusReport() {
        List<ReportDTO> report = reportService.getAppointmentStatusReport();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Thống kê tải trọng khám bệnh của các bác sĩ",
            description = "Lấy báo cáo số lượng lịch hẹn đã hoàn thành của mỗi bác sĩ.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/doctor-load")
    public ResponseEntity<List<ReportDTO>> getDoctorLoadReport() {
        List<ReportDTO> report = reportService.getDoctorLoadReport();
        return ResponseEntity.ok(report);
    }

    // THÊM ENDPOINT NÀY
    @Operation(summary = "Thống kê lịch hẹn theo ngày",
            description = "Lấy báo cáo số lượng lịch hẹn (Đã khám, Đã Hủy, Tổng) trong một ngày cụ thể.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/daily-report")
    public ResponseEntity<List<ReportDTO>> getDailyReport(
            @Parameter(description = "Ngày cần thống kê (định dạng YYYY-MM-DD)", required = true)
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ReportDTO> report = reportService.getDailyAppointmentReport(date);
        return ResponseEntity.ok(report);
    }

    // THÊM ENDPOINT NÀY
    @Operation(summary = "Thống kê tải trọng khám bệnh theo chuyên khoa",
            description = "Lấy báo cáo số lượng lịch hẹn đã hoàn thành của mỗi chuyên khoa.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/specialty-load")
    public ResponseEntity<List<ReportDTO>> getSpecialtyLoadReport() {
        List<ReportDTO> report = reportService.getAppointmentsBySpecialtyReport();
        return ResponseEntity.ok(report);
    }
}
