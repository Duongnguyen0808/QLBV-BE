package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AdminUpdateUserRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.UserResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Role;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.services.AdminService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users") // Chỉ xử lý các URL bắt đầu bằng /admin/users
@RequiredArgsConstructor
public class AdminUserWebController {

    private final AdminService adminService;

    @GetMapping
    public String showUsersListPage(Model model) {
        List<User> users = adminService.getAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(UserResponseDTO::fromUser)
                .collect(Collectors.toList());

        model.addAttribute("users", userDTOs);
        model.addAttribute("contentView", "admin/users-list");
        return "fragments/layout";
    }

    @GetMapping("/{id}")
    public String showUserDetailsPage(@PathVariable Long id, Model model) {
        User user = adminService.getUserById(id);

        model.addAttribute("user", UserResponseDTO.fromUser(user));
        model.addAttribute("allRoles", Role.values());
        model.addAttribute("allSpecialties", adminService.getAllSpecialties()); // THÊM DÒNG NÀY
        model.addAttribute("contentView", "admin/user-details");
        return "fragments/layout";
    }

    @PostMapping("/{id}/role")
    public String updateUserRole(@PathVariable Long id, @ModelAttribute AdminUpdateUserRequestDTO requestDTO, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUserRole(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.lockOrUnlockUser(id, true);
        redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.lockOrUnlockUser(id, false);
        redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản thành công!");
        return "redirect:/admin/users";
    }
}