package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.dtos.SpecialtyDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;
import vn.hcm.nhidong2.clinicbookingapi.services.AdminService;

import java.util.List;

@Controller
@RequestMapping("/admin/specialties") // Chỉ xử lý các URL bắt đầu bằng /admin/specialties
@RequiredArgsConstructor
public class AdminSpecialtyWebController {

    private final AdminService adminService;

    @GetMapping
    public String showSpecialtiesPage(Model model) {
        List<Specialty> specialties = adminService.getAllSpecialties();
        model.addAttribute("specialties", specialties);
        model.addAttribute("newSpecialty", new SpecialtyDTO());
        model.addAttribute("contentView", "admin/specialties");
        return "fragments/layout";
    }

    @PostMapping("/create")
    public String createSpecialty(@ModelAttribute SpecialtyDTO newSpecialty, RedirectAttributes ra) {
        try {
            adminService.createSpecialty(newSpecialty);
            ra.addFlashAttribute("successMessage", "Tạo chuyên khoa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/specialties";
    }

    @PostMapping("/{id}/delete")
    public String deleteSpecialty(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminService.deleteSpecialty(id);
            ra.addFlashAttribute("successMessage", "Xóa chuyên khoa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/specialties";
    }
}