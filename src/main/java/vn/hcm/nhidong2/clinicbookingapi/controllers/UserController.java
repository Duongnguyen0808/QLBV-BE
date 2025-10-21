package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ChangePasswordRequest;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PatientProfileUpdateDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.UserResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.services.AuthenticationService;
import vn.hcm.nhidong2.clinicbookingapi.services.PatientProfileService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "Các API cho người dùng đã xác thực")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthenticationService authenticationService;
    private final PatientProfileService patientProfileService;

    @Operation(summary = "Lấy thông tin cá nhân", description = "Lấy thông tin chi tiết của người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chưa đăng nhập)")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        UserResponseDTO userProfile = authenticationService.getMyProfile();
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<String> updateMyProfile(@RequestBody PatientProfileUpdateDTO dto) {
        try {
            patientProfileService.updateMyProfile(dto);
            return ResponseEntity.ok("Cập nhật hồ sơ thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Đổi mật khẩu", description = "Người dùng tự đổi mật khẩu của chính mình.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc mật khẩu hiện tại không đúng"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PutMapping("/me/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            authenticationService.changePassword(request);
            return ResponseEntity.ok("Đổi mật khẩu thành công.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
