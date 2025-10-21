package vn.hcm.nhidong2.clinicbookingapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck() {
        return "API is up and running!";
    }

}
