package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultController {
    @GetMapping("/default")
    public String defaultAfterLogin(HttpServletRequest request) {
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }
        if (request.isUserInRole("ROLE_DOCTOR")) {
            return "redirect:/doctor/appointments";
        }

        return "redirect:/login?error";
    }
}