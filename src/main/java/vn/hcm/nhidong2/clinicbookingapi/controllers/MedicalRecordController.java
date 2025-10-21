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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcm.nhidong2.clinicbookingapi.dtos.MedicalRecordRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.MedicalRecordResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.MedicalRecord;
import vn.hcm.nhidong2.clinicbookingapi.services.MedicalRecordService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Record API", description = "API quản lý lịch sử khám bệnh")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Operation(summary = "Bác sĩ tạo bệnh án mới", description = "API cho bác sĩ tạo bệnh án sau khi một lịch hẹn kết thúc. Yêu cầu quyền DOCTOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo bệnh án thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc lịch hẹn đã có bệnh án"),
            @ApiResponse(responseCode = "403", description = "Không có quyền tạo bệnh án cho lịch hẹn này")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecordResponseDTO> createMedicalRecord(@Valid @RequestBody MedicalRecordRequestDTO requestDTO) {
        MedicalRecord newRecord = medicalRecordService.createMedicalRecord(requestDTO);
        return new ResponseEntity<>(MedicalRecordResponseDTO.fromMedicalRecord(newRecord), HttpStatus.CREATED);
    }

    @Operation(summary = "Bệnh nhân xem lịch sử khám bệnh của mình", description = "API cho bệnh nhân đang đăng nhập xem lại tất cả bệnh án của mình.")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Thành công"))
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<List<MedicalRecordResponseDTO>> getMyMedicalRecords() {
        List<MedicalRecord> records = medicalRecordService.getMyMedicalRecords();
        List<MedicalRecordResponseDTO> responseDTOs = records.stream()
                .map(MedicalRecordResponseDTO::fromMedicalRecord)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }
}
