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
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users") // Chỉ xử lý các URL bắt đầu bằng /admin/users
@RequiredArgsConstructor
public class AdminUserWebController {

    private final AdminService adminService;
    
    // Ánh xạ vai trò sang tiếng Việt
    private static final Map<Role, String> ROLE_TRANSLATIONS = Map.of(
        Role.ADMIN, "Quản trị viên",
        Role.DOCTOR, "Bác sĩ",
        Role.PATIENT, "Bệnh nhân"
    );

    @GetMapping
    public String showUsersListPage(Model model) {
        List<User> users = adminService.getAllUsers();
        
        // Tạo danh sách DTO đã được định dạng
        List<Map<String, Object>> formattedUsers = users.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    // BỎ ID khỏi Map nếu bạn không muốn hiển thị
                    map.put("fullName", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole().name()); // Giữ tên Role gốc để phân loại màu sắc trong View
                    map.put("translatedRole", ROLE_TRANSLATIONS.getOrDefault(user.getRole(), user.getRole().name()));
                    map.put("isLocked", user.isLocked());
                    map.put("id", user.getId()); // Giữ ID cho nút Chi tiết
                    return map;
                })
                .collect(Collectors.toList());


        model.addAttribute("users", formattedUsers);
        model.addAttribute("contentView", "admin/users-list");
        return "fragments/layout";
    }

    @GetMapping("/{id}")
    public String showUserDetailsPage(@PathVariable Long id, Model model) {
        User user = adminService.getUserById(id);

        model.addAttribute("user", UserResponseDTO.fromUser(user));
        // THÊM: Truyền Map dịch thuật cho View chi tiết
        model.addAttribute("roleTranslations", ROLE_TRANSLATIONS); 
        model.addAttribute("allRoles", Role.values());
        model.addAttribute("allSpecialties", adminService.getAllSpecialties()); 
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