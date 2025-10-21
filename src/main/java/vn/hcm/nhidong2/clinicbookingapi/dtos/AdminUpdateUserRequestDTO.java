package vn.hcm.nhidong2.clinicbookingapi.dtos;

import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.Role;
import jakarta.validation.constraints.NotNull;

@Data
public class AdminUpdateUserRequestDTO {

    @NotNull(message = "Vai trò mới không được để trống")
    private Role newRole;

    private Long specialtyId;
}