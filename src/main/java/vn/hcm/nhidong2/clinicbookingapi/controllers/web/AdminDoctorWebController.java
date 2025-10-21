package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AssignSpecialtyRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorCreationRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;
import vn.hcm.nhidong2.clinicbookingapi.services.AdminService;

import java.util.List;

@Controller
@RequestMapping("/admin/doctors") // Chỉ xử lý các URL bắt đầu bằng /admin/doctors
@RequiredArgsConstructor
public class AdminDoctorWebController {

    private final AdminService adminService;

    @GetMapping
    public String showDoctorsListPage(Model model) {
        List<Doctor> doctors = adminService.getAllDoctors();
        List<DoctorResponseDTO> doctorDTOs = doctors.stream()
                .map(DoctorResponseDTO::fromDoctor).toList();

        model.addAttribute("doctors", doctorDTOs);
        model.addAttribute("contentView", "admin/doctors-list");
        return "fragments/layout";
    }

    @GetMapping("/create")
    public String showCreateDoctorPage(Model model) {
        model.addAttribute("newDoctorRequest", new DoctorCreationRequestDTO());
        model.addAttribute("availableUsers", adminService.getUsersWithoutDoctorProfile());
        model.addAttribute("specialties", adminService.getAllSpecialties());
        model.addAttribute("contentView", "admin/doctor-create");
        return "fragments/layout";
    }

    @PostMapping("/create")
    public String createDoctorProfile(@ModelAttribute DoctorCreationRequestDTO newDoctorRequest, RedirectAttributes ra) {
        try {
            adminService.createDoctorProfile(newDoctorRequest);
            ra.addFlashAttribute("successMessage", "Tạo hồ sơ bác sĩ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    @GetMapping("/{id}/edit")
    public String showEditDoctorPage(@PathVariable("id") Long doctorId, Model model) {
        Doctor doctor = adminService.getDoctorProfileById(doctorId);
        List<Specialty> specialties = adminService.getAllSpecialties();

        model.addAttribute("doctor", doctor);
        model.addAttribute("allSpecialties", specialties);
        model.addAttribute("updateRequest", new AssignSpecialtyRequestDTO());
        model.addAttribute("contentView", "admin/doctor-edit");
        return "fragments/layout";
    }

    @PostMapping("/{id}/edit")
    public String updateDoctorSpecialty(@PathVariable("id") Long doctorId, @ModelAttribute AssignSpecialtyRequestDTO request, RedirectAttributes ra) {
        try {
            adminService.updateDoctorSpecialty(doctorId, request);
            ra.addFlashAttribute("successMessage", "Cập nhật chuyên khoa cho bác sĩ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }
}