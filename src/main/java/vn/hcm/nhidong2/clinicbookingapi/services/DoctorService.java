package vn.hcm.nhidong2.clinicbookingapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AssignSpecialtyRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorProfileStatusDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.*;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.SpecialtyRepository;

import javax.print.Doc;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AuthenticationService authenticationService;
    private final AppointmentRepository appointmentRepository;
    private static final int APPOINTMENT_DURATION_MINUTES = 30;

    @Transactional
    public Doctor assignMySpecialty(AssignSpecialtyRequestDTO request) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();

        if (doctorRepository.findByUser(currentUser).isPresent()) {
            throw new IllegalStateException("Bạn đã có hồ sơ bác sĩ. Chỉ Admin mới có thể thay đổi chuyên khoa.");
        }

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên khoa với ID: " + request.getSpecialtyId()));

        Doctor doctorProfile = Doctor.builder()
                .user(currentUser)
                .specialty(specialty)
                .build();

        return doctorRepository.save(doctorProfile);
    }

    // Kiểm tra trạng thái hồ sơ của bác sĩ đang đăng nhập.
    public DoctorProfileStatusDTO checkMyProfileStatus() {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();

        Optional<Doctor> doctorProfileOptional = doctorRepository.findByUser(currentUser);

        if (doctorProfileOptional.isPresent()) {
            Doctor doctor = doctorProfileOptional.get();
            return DoctorProfileStatusDTO.builder()
                    .hasProfile(true)
                    .doctorProfile(DoctorResponseDTO.fromDoctor(doctor))
                    .build();
        } else {
            return DoctorProfileStatusDTO.builder()
                    .hasProfile(false)
                    .doctorProfile(null)
                    .build();
        }
    }

    // Tìm kiếm các bác sĩ khả dụng dựa trên các tiêu chí lọc.
    public List<Doctor> findAvailableDoctors(Optional<Long> specialtyId, Optional<OffsetDateTime> dateTime) {
        if (specialtyId.isEmpty() && dateTime.isEmpty()) {
            return Collections.emptyList();
        }

        List<Doctor> doctorsToFilter;

        if (specialtyId.isPresent()) {
            doctorsToFilter = doctorRepository.findAllBySpecialtyId(specialtyId.get());
        } else {
            doctorsToFilter = doctorRepository.findAll();
        }

        if (dateTime.isPresent()) {
            OffsetDateTime requestedTime = dateTime.get();

            return doctorsToFilter.stream()
                    .filter(doctor -> isDoctorAvailable(doctor.getId(), requestedTime))
                    .collect(Collectors.toList());
        }

        return doctorsToFilter;
    }

    public List<Doctor> searchDoctors(Optional<String> name, Optional<Long> specialtyId, Optional<DayOfWeek> dayOfWeek) {
        List<Doctor> result = doctorRepository.findAll();

        // Lọc theo tên
        if (name.isPresent() && !name.get().isBlank()) {
            String keyword = name.get().toLowerCase();
            result = result.stream()
                    .filter(doctor -> doctor.getUser().getFullName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        // Lọc theo chuyên khoa
        if (specialtyId.isPresent()) {
            result = result.stream()
                    .filter(doctor -> doctor.getSpecialty().getId().equals(specialtyId.get()))
                    .collect(Collectors.toList());
        }

        // Lọc theo ngày làm việc
        if (dayOfWeek.isPresent()) {
            result = result.stream()
                    .filter(doctor -> doctor.getWorkingSchedules().stream()
                            .anyMatch(schedule -> schedule.getDayOfWeek() == dayOfWeek.get()))
                    .collect(Collectors.toList());
        }

        return result;
    }

    private boolean isDoctorAvailable(Long doctorId, OffsetDateTime requestedTime) {
        OffsetDateTime startTime = requestedTime.minusMinutes(APPOINTMENT_DURATION_MINUTES - 1);
        OffsetDateTime endTime = requestedTime.plusMinutes(APPOINTMENT_DURATION_MINUTES - 1);

        List<Appointment> overlappingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateTimeBetweenAndStatusNot(
                        doctorId, startTime, endTime, AppointmentStatus.CANCELLED);

        return overlappingAppointments.isEmpty();
    }
}
