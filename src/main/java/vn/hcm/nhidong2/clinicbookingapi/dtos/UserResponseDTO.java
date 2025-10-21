package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.Gender;
import vn.hcm.nhidong2.clinicbookingapi.models.PatientProfile;
import vn.hcm.nhidong2.clinicbookingapi.models.Role;
import vn.hcm.nhidong2.clinicbookingapi.models.User;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Đối tượng trả về chứa thông tin công khai của người dùng")
public class UserResponseDTO {

    @Schema(description = "ID duy nhất của người dùng", example = "1")
    private Long id;

    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Địa chỉ email", example = "nguyenvana@example.com")
    private String email;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phoneNumber;

    @Schema(description = "Vai trò của người dùng trong hệ thống", example = "PATIENT")
    private Role role;

    @Schema(description = "Trạng thái kích hoạt tài khoản (true = đã kích hoạt)", example = "true")
    private boolean enabled;

    @Schema(description = "Trạng thái khóa tài khoản (true = đã bị khóa)", example = "false")
    private boolean locked;

    @Schema(description = "Thời điểm tài khoản được tạo", example = "2025-09-10T10:30:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Ngày sinh", example = "2000-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính", example = "MALE")
    private Gender gender;

    @Schema(description = "Địa chỉ", example = "123 Đường ABC, Quận 1, TP.HCM")
    private String address;

    @Schema(description = "Tiền sử bệnh án", example = "Từng bị viêm phổi lúc nhỏ")
    private String medicalHistory;

    @Schema(description = "Dị ứng", example = "Dị ứng với phấn hoa")
    private String allergies;

    public static UserResponseDTO fromUser(User user) {
        UserResponseDTOBuilder builder = UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .createdAt(user.getCreatedAt());

        if (user.getPatientProfile() != null) {
            PatientProfile profile = user.getPatientProfile();
            builder.dateOfBirth(profile.getDateOfBirth())
                    .gender(profile.getGender())
                    .address(profile.getAddress())
                    .medicalHistory(profile.getMedicalHistory())
                    .allergies(profile.getAllergies());
        }

        return builder.build();
    }
}
