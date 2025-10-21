package vn.hcm.nhidong2.clinicbookingapi.dtos;

import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.Gender;

import java.time.LocalDate;

@Data
public class PatientProfileUpdateDTO {
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String medicalHistory;
    private String allergies;
    private String fullName;
    private String phoneNumber;
}
