package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorProfileStatusDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.services.AppointmentService;
import vn.hcm.nhidong2.clinicbookingapi.services.DoctorService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor API", description = "Các API cho bác sĩ tự thao tác (yêu cầu quyền DOCTOR)")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @Operation(summary = "Kiểm tra trạng thái hồ sơ của bác sĩ",
            description = "API cho bác sĩ đang đăng nhập kiểm tra xem mình đã có hồ sơ và chuyên khoa hay chưa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kiểm tra thành công"),
            @ApiResponse(responseCode = "403", description = "Yêu cầu quyền DOCTOR")
    })
    @GetMapping("/me/profile-status")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')") // Đảm bảo chỉ bác sĩ mới gọi được
    public ResponseEntity<DoctorProfileStatusDTO> getMyProfileStatus() {
        DoctorProfileStatusDTO status = doctorService.checkMyProfileStatus();
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Tìm kiếm bác sĩ khả dụng",
            description = "API công khai để tìm bác sĩ theo chuyên khoa và/hoặc thời gian. " +
                    "Nếu không có filter nào, sẽ trả về danh sách rỗng.")
    @GetMapping("/available")
    public ResponseEntity<List<DoctorResponseDTO>> findAvailableDoctors(
            @Parameter(description = "ID của chuyên khoa muốn lọc", example = "1")
            @RequestParam(required = false) Optional<Long> specialtyId,

            @Parameter(description = "Thời gian muốn kiểm tra lịch trống (định dạng ISO: 2025-10-22T10:30:00+07:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<OffsetDateTime> dateTime
    ) {
        List<Doctor> availableDoctors = doctorService.findAvailableDoctors(specialtyId, dateTime);
        List<DoctorResponseDTO> doctorDTOs = availableDoctors.stream()
                .map(DoctorResponseDTO::fromDoctor)
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctorDTOs);
    }

    @Operation(summary = "Bác sĩ xem lịch hẹn của mình",
            description = "Lấy danh sách tất cả các lịch hẹn đã được đặt với bác sĩ đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "403", description = "Yêu cầu quyền DOCTOR")
    })
    @GetMapping("/me/appointments")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointments() {
        List<AppointmentResponseDTO> appointments = appointmentService.findAppointmentsForDoctor();
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Gửi nhắc lịch cho bệnh nhân")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đã gửi nhắc lịch thành công"),
            @ApiResponse(responseCode = "400", description = "Lỗi xử lý yêu cầu"),
            @ApiResponse(responseCode = "403", description = "Yêu cầu quyền DOCTOR")
    })
    @PostMapping("/appointments/{id}/remind")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<Map<String, Object>> remindAppointment(@PathVariable("id") Long appointmentId) {
        try {
            appointmentService.sendReminder(appointmentId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi nhắc lịch thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
}
