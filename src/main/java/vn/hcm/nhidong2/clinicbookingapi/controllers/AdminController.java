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
import vn.hcm.nhidong2.clinicbookingapi.dtos.*;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.services.AdminService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API", description = "Các API quản trị người dùng (yêu cầu quyền ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Lấy danh sách tất cả người dùng", description = "API cho Admin để xem tất cả người dùng trong hệ thống.")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Thành công"))
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        List<UserResponseDTO> userResponseDTOS = users.stream()
                .map(UserResponseDTO::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponseDTOS);
    }

    @Operation(summary = "Lấy thông tin một người dùng theo ID", description = "API cho Admin để xem chi tiết một người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Không tìm thấy người dùng với ID cung cấp")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = adminService.getUserById(id);
            return ResponseEntity.ok(UserResponseDTO.fromUser(user));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Admin cập nhật vai trò cho người dùng", description = "API cho Admin để thay đổi vai trò của một người dùng (vd: PATIENT -> DOCTOR).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật vai trò thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc không tìm thấy người dùng"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequestDTO requestDTO
    ) {
        try {
            User updatedUser = adminService.updateUserRole(id, requestDTO);
            return ResponseEntity.ok(UserResponseDTO.fromUser(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API để khóa tài khoản người dùng
    @Operation(summary = "Khóa tài khoản người dùng", description = "API cho Admin để khóa tài khoản, không cho phép đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Khóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không tìm thấy người dùng")
    })
    @PutMapping("/users/{id}/lock")
    public ResponseEntity<String> lockUser(@PathVariable Long id) {
        try {
            adminService.lockOrUnlockUser(id, true);
            return ResponseEntity.ok("Khóa tài khoản thành công.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API để mở khóa tài khoản người dùng
    @Operation(summary = "Mở khóa tài khoản người dùng", description = "API cho Admin để mở khóa một tài khoản đã bị khóa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mở khóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không tìm thấy người dùng")
    })
    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<String> unlockUser(@PathVariable Long id) {
        try {
            adminService.lockOrUnlockUser(id, false);
            return ResponseEntity.ok("Mở khóa tài khoản thành công.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Specialty Management Endpoints

    @Operation(summary = "Tạo một chuyên khoa mới", description = "API cho Admin để tạo chuyên khoa mới.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo chuyên khoa thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc tên chuyên khoa đã tồn tại")
    })
    @PostMapping("/specialties")
    public ResponseEntity<SpecialtyDTO> createSpecialty(@Valid @RequestBody SpecialtyDTO specialtyDTO) {
        Specialty newSpecialty = adminService.createSpecialty(specialtyDTO);
        return new ResponseEntity<>(SpecialtyDTO.fromSpecialty(newSpecialty), HttpStatus.CREATED);
    }

    @Operation(summary = "Admin cập nhật tên chuyên khoa", description = "API cho Admin để thay đổi tên của một chuyên khoa đã có.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc tên chuyên khoa đã tồn tại"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyên khoa")
    })
    @PutMapping("/specialties/{id}")
    public ResponseEntity<?> updateSpecialty(@PathVariable Long id, @Valid @RequestBody SpecialtyDTO specialtyDTO) {
        try {
            Specialty updatedSpecialty = adminService.updateSpecialty(id, specialtyDTO);
            return ResponseEntity.ok(SpecialtyDTO.fromSpecialty(updatedSpecialty));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Admin xóa một chuyên khoa", description = "API cho Admin để xóa một chuyên khoa. Chỉ xóa được khi không còn bác sĩ nào thuộc chuyên khoa đó.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa vì vẫn còn bác sĩ trong chuyên khoa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyên khoa")
    })
    @DeleteMapping("/specialties/{id}")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable Long id) {
        try {
            adminService.deleteSpecialty(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Doctor Management Endpoints

    @Operation(summary = "Tạo hồ sơ bác sĩ và gán chuyên khoa", description = "API cho Admin để gán vai trò bác sĩ và chuyên khoa cho một tài khoản User.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo hồ sơ bác sĩ thành công"),
            @ApiResponse(responseCode = "400", description = "User/Chuyên khoa không tồn tại, hoặc user đã là bác sĩ")
    })
    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponseDTO> createDoctorProfile(@Valid @RequestBody DoctorCreationRequestDTO request) {
        Doctor newDoctorProfile = adminService.createDoctorProfile(request);
        return new ResponseEntity<>(DoctorResponseDTO.fromDoctor(newDoctorProfile), HttpStatus.CREATED);
    }

    @Operation(summary = "Admin cập nhật chuyên khoa cho bác sĩ", description = "API cho Admin để thay đổi chuyên khoa của một hồ sơ bác sĩ đã có.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Hồ sơ bác sĩ hoặc chuyên khoa không tồn tại")
    })
    @PutMapping("/doctors/{id}/specialty")
    public ResponseEntity<DoctorResponseDTO> updateDoctorSpecialty(
            @PathVariable Long id,
            @Valid @RequestBody AssignSpecialtyRequestDTO request
    ) {
        Doctor updatedDoctor = adminService.updateDoctorSpecialty(id, request);
        return ResponseEntity.ok(DoctorResponseDTO.fromDoctor(updatedDoctor));
    }
}
