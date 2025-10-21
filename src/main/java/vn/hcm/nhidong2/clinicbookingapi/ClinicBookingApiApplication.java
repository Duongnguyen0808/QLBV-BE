package vn.hcm.nhidong2.clinicbookingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ClinicBookingApiApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClinicBookingApiApplication.class)
                .properties("spring.config.additional-location=optional:classpath:application-secrets.properties")
                .run(args);
    }

}
