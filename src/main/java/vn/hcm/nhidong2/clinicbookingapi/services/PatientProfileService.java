package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PatientProfileUpdateDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.PatientProfile;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.repositories.PatientProfileRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class PatientProfileService {

    private final PatientProfileRepository profileRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Transactional
    public PatientProfile updateMyProfile(PatientProfileUpdateDTO dto) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        PatientProfile profile = profileRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ bệnh nhân."));

        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setAddress(dto.getAddress());
        profile.setMedicalHistory(dto.getMedicalHistory());
        profile.setAllergies(dto.getAllergies());

        currentUser.setFullName(dto.getFullName());
        currentUser.setPhoneNumber(dto.getPhoneNumber());
        userRepository.save(currentUser);

        return profileRepository.save(profile);
    }
}
