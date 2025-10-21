package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentUpdateDTO;
import vn.hcm.nhidong2.clinicbookingapi.services.AppointmentService;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment API", description = "Các API để quản lý lịch hẹn của bệnh nhân")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Tạo một lịch hẹn mới",
            description = "Bệnh nhân tạo lịch hẹn. Cần cung cấp specialtyId (bắt buộc) và có thể cung cấp doctorId (tùy chọn).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo lịch hẹn thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (vd: bác sĩ không thuộc chuyên khoa, không có bác sĩ trống lịch)")
    })
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO requestDTO) {
        AppointmentResponseDTO newAppointmentDTO = appointmentService.createAppointment(requestDTO);
        return new ResponseEntity<>(newAppointmentDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy danh sách lịch hẹn của tôi", description = "Lấy tất cả các lịch hẹn của bệnh nhân đang đăng nhập.")
    @GetMapping("/me")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointments() {
        // Service đã trả về List<DTO>, chỉ cần đưa vào response
        List<AppointmentResponseDTO> responseDTOs = appointmentService.findAppointmentsForPatient();
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "Đổi ngày giờ của một lịch hẹn", description = "Bệnh nhân thay đổi thời gian của một lịch hẹn đã tạo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền sửa lịch hẹn này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch hẹn")
    })
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentUpdateDTO updateDTO) {
        // Service đã trả về DTO, chỉ cần đưa vào response
        AppointmentResponseDTO updatedAppointmentDTO = appointmentService.updateAppointmentDateTime(id, updateDTO);
        return ResponseEntity.ok(updatedAppointmentDTO);
    }

    @Operation(summary = "Hủy một lịch hẹn", description = "Bệnh nhân hủy một lịch hẹn đã tạo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hủy thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền hủy lịch hẹn này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch hẹn")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(@PathVariable Long id) {
        // Service đã trả về DTO, chỉ cần đưa vào response
        AppointmentResponseDTO cancelledAppointmentDTO = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(cancelledAppointmentDTO);
    }
}
