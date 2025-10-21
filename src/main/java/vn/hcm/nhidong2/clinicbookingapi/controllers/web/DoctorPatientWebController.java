package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.MedicalRecord;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.UserRepository;
import vn.hcm.nhidong2.clinicbookingapi.services.AuthenticationService;
import vn.hcm.nhidong2.clinicbookingapi.services.MedicalRecordService;

import java.util.List;

@Controller
@RequestMapping("/doctor/patients")
@RequiredArgsConstructor
public class DoctorPatientWebController {

    private final AuthenticationService authenticationService;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordService medicalRecordService;
    private final UserRepository userRepository;

    // Trang danh sách bệnh nhân
    @GetMapping
    public String showMyPatientsPage(Model model) {
        User currentDoctorUser = authenticationService.getCurrentAuthenticatedUser();
        Doctor doctorProfile = doctorRepository.findByUser(currentDoctorUser).orElseThrow();
        List<User> patients = appointmentRepository.findDistinctPatientsByDoctorId(doctorProfile.getId());

        model.addAttribute("patients", patients);
        model.addAttribute("contentView", "doctor/patients-list");
        return "fragments/layout";
    }

    // Trang xem lịch sử khám bệnh của một bệnh nhân
    @GetMapping("/{patientId}/history")
    public String showPatientHistoryPage(@PathVariable Long patientId, Model model) {
        try {
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsForPatientByDoctor(patientId);
            User patient = userRepository.findById(patientId).orElseThrow();
            model.addAttribute("records", records);
            model.addAttribute("patient", patient);
            model.addAttribute("contentView", "doctor/patient-history");
            return "fragments/layout";
        } catch (Exception e) {
            return "redirect:/doctor/patients";
        }
    }
}
