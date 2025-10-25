package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.*;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.Role;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.SpecialtyRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.UserRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.PatientProfileRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.WorkingScheduleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final AuthenticationService authenticationService;
    // THÊM: Inject các repository cần cho xóa user
    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final WorkingScheduleRepository workingScheduleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User lockOrUnlockUser(Long userId, boolean lock) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng với ID: " + userId));

        user.setLocked(lock);
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng với ID: " + userId));
    }

    @Transactional
    public User updateUserRole(Long userId, AdminUpdateUserRequestDTO requestDTO) {
        User adminUser = authenticationService.getCurrentAuthenticatedUser();

        if (java.util.Objects.equals(adminUser.getId(), userId)) {
            throw new IllegalArgumentException("Admin không thể tự thay đổi vai trò của chính mình.");
        }

        User userToUpdate = getUserById(userId);

        // Nếu người dùng đã là bác sĩ, không cho phép thay đổi vai trò từ đây.
        if (userToUpdate.getRole() == Role.DOCTOR) {
            throw new IllegalArgumentException("Không thể thay đổi vai trò của Bác sĩ. Vui lòng dùng trang Quản lý Bác sĩ.");
        }

        // Xử lý khi vai trò mới là DOCTOR
        if (requestDTO.getNewRole() == Role.DOCTOR) {
            if (requestDTO.getSpecialtyId() == null) {
                throw new IllegalArgumentException("Phải chọn một chuyên khoa khi thăng cấp người dùng thành Bác sĩ.");
            }
            DoctorCreationRequestDTO doctorRequest = new DoctorCreationRequestDTO();
            doctorRequest.setUserId(userId);
            doctorRequest.setSpecialtyId(requestDTO.getSpecialtyId());
            createDoctorProfile(doctorRequest);
        } else {
            userToUpdate.setRole(requestDTO.getNewRole());
        }

        return userRepository.save(userToUpdate);
    }


    // QUẢN LÝ CHUYÊN KHOA

    // Tạo một chuyên khoa mới
    public Specialty createSpecialty(SpecialtyDTO specialtyDTO) {
        // Kiểm tra xem tên chuyên khoa đã tồn tại chưa
        if (specialtyRepository.findByName(specialtyDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại.");
        }

        Specialty specialty = Specialty.builder()
                .name(specialtyDTO.getName())
                .build();

        return specialtyRepository.save(specialty);
    }

    // Sửa một chuyên khoa
    @Transactional
    public Specialty updateSpecialty(Long specialtyId, SpecialtyDTO specialtyDTO) {
        Specialty existingSpecialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy chuyên khoa với ID: " + specialtyId));

        specialtyRepository.findByName(specialtyDTO.getName())
                .ifPresent(foundSpecialty -> {
                    if (!foundSpecialty.getId().equals(specialtyId)) {
                        throw new IllegalArgumentException("Tên chuyên khoa đã tồn tại.");
                    }
                });

        existingSpecialty.setName(specialtyDTO.getName());
        return specialtyRepository.save(existingSpecialty);
    }

    // Xóa một chuyên khoa
    @Transactional
    public void deleteSpecialty(Long specialtyId) {
        if (!specialtyRepository.existsById(specialtyId)) {
            throw new IllegalStateException("Không tìm thấy chuyên khoa với ID: " + specialtyId);
        }

        if (doctorRepository.existsBySpecialtyId(specialtyId)) {
            throw new IllegalStateException("Không thể xóa chuyên khoa vì vẫn còn bác sĩ thuộc chuyên khoa này.");
        }

        specialtyRepository.deleteById(specialtyId);
    }

    // Lấy danh sách tất cả các chuyên khoa.
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    // QUẢN LÝ BÁC SĨ

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorProfileById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bác sĩ với ID: " + doctorId));
    }

    public List<User> getUsersWithoutDoctorProfile() {
        // Lấy ID của tất cả các user đã là bác sĩ
        List<Long> doctorUserIds = doctorRepository.findAll().stream()
                .map(doctor -> doctor.getUser().getId())
                .toList();

        // Lấy tất cả user và lọc ra những ai chưa có trong danh sách trên
        return userRepository.findAll().stream()
                .filter(user -> !doctorUserIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    // Tạo hồ sơ bác sĩ và gán chuyên khoa cho một tài khoản User.
    @Transactional
    public Doctor createDoctorProfile(DoctorCreationRequestDTO request) {
        User user = getUserById(request.getUserId());

        if (doctorRepository.findByUser(user).isPresent()) {
            throw new IllegalArgumentException("Tài khoản này đã được gán hồ sơ bác sĩ.");
        }

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy chuyên khoa với ID: " + request.getSpecialtyId()));

        if (user.getRole() != Role.DOCTOR) {
            user.setRole(Role.DOCTOR);
            userRepository.save(user);
        }

        Doctor doctorProfile = Doctor.builder()
                .user(user)
                .specialty(specialty)
                .build();
        return doctorRepository.save(doctorProfile);
    }

    @Transactional
    public Doctor updateDoctorSpecialty(Long doctorId, AssignSpecialtyRequestDTO request) {
        Doctor doctorProfile = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bác sĩ với ID: " + doctorId));

        Specialty newSpecialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyên khoa với ID: " + request.getSpecialtyId()));

        doctorProfile.setSpecialty(newSpecialty);
        return doctorRepository.save(doctorProfile);
    }

    public List<Doctor> findDoctorsBySpecialty(Long specialtyId) {
        return doctorRepository.findAllBySpecialtyId(specialtyId);
    }

    // ========================
    // XÓA NGƯỜI DÙNG (ADMIN)
    // ========================
    @Transactional
    public void deleteUser(Long userId) {
        User actingAdmin = authenticationService.getCurrentAuthenticatedUser();
        if (java.util.Objects.equals(actingAdmin.getId(), userId)) {
            throw new IllegalArgumentException("Không thể xóa chính tài khoản Admin đang đăng nhập.");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng với ID: " + userId));

        if (target.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Không được xóa tài khoản Admin.");
        }

        // Kiểm tra và xử lý xóa bác sĩ
        Optional<Doctor> doctorProfile = doctorRepository.findByUser(target);
        if (doctorProfile.isPresent()) {
            Doctor doctor = doctorProfile.get();
            
            // Kiểm tra xem bác sĩ có lịch hẹn nào không
            List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctor.getId());
            if (!doctorAppointments.isEmpty()) {
                throw new IllegalArgumentException("Không thể xóa bác sĩ vì vẫn còn lịch hẹn.");
            }
            
            // Xóa lịch làm việc của bác sĩ
            List<WorkingSchedule> schedules = workingScheduleRepository.findByDoctorId(doctor.getId());
            workingScheduleRepository.deleteAll(schedules);
            
            // Xóa hồ sơ bác sĩ
            doctorRepository.delete(doctor);
        }

        // Nếu là bệnh nhân và còn lịch hẹn, không cho xóa để tránh mồ côi dữ liệu
        if (!appointmentRepository.findByPatient(target).isEmpty()) {
            throw new IllegalArgumentException("Không thể xóa vì người dùng vẫn còn lịch hẹn.");
        }

        // Xóa hồ sơ bệnh nhân nếu tồn tại (tránh lỗi khóa ngoại)
        patientProfileRepository.findById(userId).ifPresent(pp -> patientProfileRepository.deleteById(userId));

        // Thực hiện xóa tài khoản
        userRepository.deleteById(userId);
    }
}
