package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.SpecialtyDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;
import vn.hcm.nhidong2.clinicbookingapi.services.AdminService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Tag(name = "Specialty API", description = "Các API công khai liên quan đến chuyên khoa")
public class SpecialtyController {

    private final AdminService adminService;

    @Operation(summary = "Lấy danh sách tất cả chuyên khoa", description = "API công khai để bất kỳ ai cũng có thể xem danh sách chuyên khoa.")
    @GetMapping
    public ResponseEntity<List<SpecialtyDTO>> getAllSpecialties() {
        List<Specialty> specialties = adminService.getAllSpecialties();
        List<SpecialtyDTO> specialtyDTOS = specialties.stream()
                .map(SpecialtyDTO::fromSpecialty)
                .collect(Collectors.toList());
        return ResponseEntity.ok(specialtyDTOS);
    }

    @Operation(summary = "Lấy danh sách bác sĩ theo chuyên khoa", description = "API công khai để lấy danh sách các bác sĩ thuộc một chuyên khoa cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyên khoa")
    })
    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<DoctorResponseDTO>> getDoctorsBySpecialty(@PathVariable Long id) {
        List<Doctor> doctors = adminService.findDoctorsBySpecialty(id);
        List<DoctorResponseDTO> doctorDTOs = doctors.stream()
                .map(DoctorResponseDTO::fromDoctor)
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctorDTOs);
    }
}
