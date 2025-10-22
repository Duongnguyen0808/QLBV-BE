package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.AppointmentUpdateDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.*;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.SpecialtyRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.WorkingScheduleRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.TransactionRepository; // ĐÃ THÊM

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;
    private final SpecialtyRepository specialtyRepository;
    private final AuthenticationService authenticationService;
    private final WorkingScheduleRepository workingScheduleRepository;
    private final TransactionRepository transactionRepository; // ĐÃ THÊM

    private static final ZoneId HOSPITAL_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final long MINIMUM_LEAD_TIME_HOURS = 1;
    private static final long MINIMUM_CANCELLATION_LEAD_TIME_HOURS = 24;
    
    // Phí mặc định đã được cập nhật
    private static final Long DEFAULT_APPOINTMENT_FEE = 5000L; 

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO requestDTO) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();

        Specialty specialty = specialtyRepository.findById(requestDTO.getSpecialtyId())
                .orElseThrow(() -> new IllegalArgumentException("Chuyên khoa không tồn tại."));

        Doctor assignedDoctor;
        // Xác định thời lượng cuộc hẹn, mặc định là 30 phút nếu không được cung cấp
        int duration = requestDTO.getDuration() != null ? requestDTO.getDuration() : 30;

        if (requestDTO.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + requestDTO.getDoctorId()));

            if (!Objects.equals(doctor.getSpecialty().getId(), specialty.getId())) {
                throw new IllegalArgumentException("Bác sĩ này không thuộc chuyên khoa đã chọn.");
            }

            validateAppointmentTimeWithSchedule(doctor, requestDTO.getAppointmentDateTime(), duration);
            // Gọi checkAppointmentOverlap với thời lượng động
            checkAppointmentOverlap(doctor.getId(), requestDTO.getAppointmentDateTime(), duration);
            assignedDoctor = doctor;
        } else {
            // Lấy danh sách bác sĩ trong chuyên khoa
            List<Doctor> doctorsInSpecialty = doctorRepository.findAllBySpecialtyId(specialty.getId());
            if (doctorsInSpecialty.isEmpty()) {
                throw new IllegalStateException("Chuyên khoa này hiện không có bác sĩ nào.");
            }

            // Lọc và tìm bác sĩ phù hợp (Logic này vẫn giữ nguyên, dùng WorkingSchedule cũ)
            assignedDoctor = doctorsInSpecialty.stream()
                    .filter(doctor -> {
                        ZonedDateTime hospitalTime = requestDTO.getAppointmentDateTime().atZoneSameInstant(HOSPITAL_ZONE_ID);
                        DayOfWeek requestedDay = hospitalTime.getDayOfWeek();
                        LocalTime requestedTime = hospitalTime.toLocalTime();

                        // TÌM TẤT CẢ CA LÀM VIỆC CỦA BÁC SĨ TRONG NGÀY ĐÓ
                        List<WorkingSchedule> schedules = workingScheduleRepository.findByDoctorId(doctor.getId()).stream()
                                .filter(s -> s.getDayOfWeek() == requestedDay)
                                .collect(Collectors.toList());

                        if (schedules.isEmpty()) {
                            return false;
                        }

                        // Kiểm tra xem giờ hẹn có nằm trong BẤT KỲ ca làm việc nào không
                        boolean isInAnyWorkingHours = schedules.stream().anyMatch(schedule ->
                            !requestedTime.isBefore(schedule.getStartTime()) &&
                            !requestedTime.plusMinutes(duration).isAfter(schedule.getEndTime())
                        );

                        if (!isInAnyWorkingHours) {
                            return false;
                        }

                        // Kiểm tra xem bác sĩ có bị trùng lịch hẹn khác không
                        return isDoctorAvailable(doctor.getId(), requestDTO.getAppointmentDateTime(), duration);
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Không có bác sĩ nào phù hợp hoặc còn trống lịch vào thời điểm này trong chuyên khoa đã chọn."));
        }

        Appointment appointment = Appointment.builder()
                .patient(currentUser)
                .doctor(assignedDoctor)
                .appointmentDateTime(requestDTO.getAppointmentDateTime())
                .notes(requestDTO.getNotes())
                .status(AppointmentStatus.PAID_PENDING) // SỬA: ĐẶT TRẠNG THÁI LÀ CHỜ THANH TOÁN
                .duration(duration)
                .amount(DEFAULT_APPOINTMENT_FEE) // Gán phí mặc định MỚI (5000L)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        try {
            String doctorEmail = savedAppointment.getDoctor().getUser().getEmail();
            String doctorName = savedAppointment.getDoctor().getUser().getFullName();
            String patientName = savedAppointment.getPatient().getFullName();
            String appointmentTime = savedAppointment.getAppointmentDateTime().toString(); // Cần format

            emailService.sendAppointmentStatusEmail(
                    doctorEmail,
                    "Bạn có lịch hẹn mới",
                    doctorName, // Gửi cho bác sĩ nên chào bác sĩ
                    patientName, // Tên bệnh nhân
                    appointmentTime,
                    "CHỜ THANH TOÁN (PAID_PENDING)" // CẬP NHẬT TRẠNG THÁI GỬI MAIL
            );
        } catch (Exception e) {
            log.error("Gửi email thông báo lịch hẹn mới thất bại cho appointment ID: " + savedAppointment.getId(), e);
        }

        return AppointmentResponseDTO.fromAppointment(savedAppointment);
    }

    @Transactional
    public AppointmentResponseDTO updateAppointmentDateTime(Long appointmentId, AppointmentUpdateDTO updateDTO) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn."));

        if (!Objects.equals(appointment.getPatient().getId(), currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa lịch hẹn này.");
        }

        validateAppointmentTimeWithSchedule(appointment.getDoctor(), updateDTO.getNewAppointmentDateTime(), appointment.getDuration());
        // Gọi checkAppointmentOverlap với thời lượng của chính lịch hẹn đó
        checkAppointmentOverlap(appointment.getDoctor().getId(), updateDTO.getNewAppointmentDateTime(), appointment.getDuration());
        appointment.setAppointmentDateTime(updateDTO.getNewAppointmentDateTime());
        appointment.setStatus(AppointmentStatus.PAID_PENDING); // ĐƯA VỀ CHỜ THANH TOÁN SAU KHI ĐỔI LỊCH

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return AppointmentResponseDTO.fromAppointment(updatedAppointment);
    }

    @Transactional
    public AppointmentResponseDTO confirmAppointment(Long appointmentId) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch hẹn."));

        boolean isDoctorOfAppointment = currentUser.getRole() == Role.DOCTOR &&
                Objects.equals(appointment.getDoctor().getUser().getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isDoctorOfAppointment && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xác nhận lịch hẹn này.");
        }

        // SỬA: Cho phép xác nhận cả lịch hẹn PENDING và PAID_PENDING
        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.PAID_PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận các lịch hẹn đang ở trạng thái 'Chờ xác nhận' hoặc 'Chờ thanh toán'.");
        }
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment confirmedAppointment = appointmentRepository.save(appointment);

        try {
            String patientEmail = confirmedAppointment.getPatient().getEmail();
            String patientName = confirmedAppointment.getPatient().getFullName();
            String doctorName = confirmedAppointment.getDoctor().getUser().getFullName();
            String appointmentTime = confirmedAppointment.getAppointmentDateTime().toString(); // Cần format đẹp hơn

            emailService.sendAppointmentStatusEmail(
                    patientEmail,
                    "Lịch hẹn của bạn đã được xác nhận",
                    patientName,
                    doctorName,
                    appointmentTime,
                    "ĐÃ XÁC NHẬN"
            );
        } catch (Exception e) {
            // Ghi log lỗi gửi email nhưng không làm ảnh hưởng đến luồng chính
            log.error("Gửi email xác nhận lịch hẹn thất bại cho appointment ID: " + confirmedAppointment.getId(), e);
        }

        return AppointmentResponseDTO.fromAppointment(confirmedAppointment);
    }

    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch hẹn."));

        boolean isOwner = Objects.equals(appointment.getPatient().getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền hủy lịch hẹn này.");
        }

        // Chỉ bệnh nhân mới bị kiểm tra quy tắc, Admin có thể hủy bất cứ lúc nào
        if (isOwner) {
            OffsetDateTime now = OffsetDateTime.now(HOSPITAL_ZONE_ID);
            long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointment.getAppointmentDateTime());

            if (hoursUntilAppointment < MINIMUM_CANCELLATION_LEAD_TIME_HOURS) {
                throw new IllegalStateException("Không thể hủy lịch hẹn trong vòng " + MINIMUM_CANCELLATION_LEAD_TIME_HOURS + " giờ trước thời điểm khám.");
            }
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể hủy lịch hẹn đã hoàn thành hoặc đã được hủy trước đó.");
        }
        
        // CẬP NHẬT: Hủy giao dịch liên quan nếu đang ở trạng thái PENDING hoặc FAILED
        transactionRepository.findByAppointmentId(appointmentId)
            .filter(t -> t.getStatus() == PaymentStatus.PENDING || t.getStatus() == PaymentStatus.FAILED)
            .ifPresent(transaction -> {
                transaction.setStatus(PaymentStatus.CANCELLED);
                transactionRepository.save(transaction);
            });
            
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment cancelledAppointment = appointmentRepository.save(appointment);

        return AppointmentResponseDTO.fromAppointment(cancelledAppointment);
    }

    public List<AppointmentResponseDTO> findAppointmentsForPatient() {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        List<Appointment> appointments = appointmentRepository.findByPatient(currentUser);

        return appointments.stream()
                .map(AppointmentResponseDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> findAppointmentsForDoctor() {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        Doctor doctorProfile = doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bác sĩ cho tài khoản này."));

        // Gọi phương thức repository đơn giản
        List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctorProfile.getId());

        // Chuyển đổi sang DTO, giờ đây fromAppointment sẽ tự xử lý medicalRecordId
        return appointments.stream()
                .map(AppointmentResponseDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    private boolean isDoctorAvailable(Long doctorId, OffsetDateTime requestedTime, int duration) {
        // Lấy tất cả các lịch hẹn chưa bị hủy của bác sĩ trong ngày hôm đó
        OffsetDateTime startOfDay = requestedTime.toLocalDate().atStartOfDay(requestedTime.getOffset()).toOffsetDateTime();
        OffsetDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        List<Appointment> appointmentsOnDay = appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetweenAndStatusNot(
                doctorId, startOfDay, endOfDay, AppointmentStatus.CANCELLED
        );

        // Định nghĩa khung giờ của lịch hẹn mới
        OffsetDateTime newAppointmentStart = requestedTime;
        OffsetDateTime newAppointmentEnd = requestedTime.plusMinutes(duration);

        // Kiểm tra xung đột với từng lịch hẹn đã có
        for (Appointment existingAppointment : appointmentsOnDay) {
            OffsetDateTime existingStart = existingAppointment.getAppointmentDateTime();
            OffsetDateTime existingEnd = existingStart.plusMinutes(existingAppointment.getDuration()); 

            // Xung đột xảy ra nếu:
            // (StartA < EndB) and (EndA > StartB)
            if (newAppointmentStart.isBefore(existingEnd) && newAppointmentEnd.isAfter(existingStart)) {
                return false;
            }
        }

        return true;
    }

    private void checkAppointmentOverlap(Long doctorId, OffsetDateTime requestedTime, int duration) {
        if (!isDoctorAvailable(doctorId, requestedTime, duration)) {
            throw new IllegalStateException("Bác sĩ đã có lịch hẹn vào thời gian này. Vui lòng chọn thời gian khác.");
        }
    }

    // THAY ĐỔI LỚN: Kiểm tra lịch làm việc theo TẤT CẢ các ca làm việc trong ngày (WorkingSchedule)
    private void validateAppointmentTimeWithSchedule(Doctor doctor, OffsetDateTime requestedTime, int duration) {
        ZonedDateTime hospitalTime = requestedTime.atZoneSameInstant(HOSPITAL_ZONE_ID);
        DayOfWeek dayOfWeek = hospitalTime.getDayOfWeek();
        LocalTime requestedTimeLocal = hospitalTime.toLocalTime();

        // 1. Kiểm tra thời gian đặt lịch phải sau thời gian hiện tại
        ZonedDateTime nowInHospitalZone = ZonedDateTime.now(HOSPITAL_ZONE_ID);
        if (hospitalTime.isBefore(nowInHospitalZone.plusHours(MINIMUM_LEAD_TIME_HOURS))) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải sau thời gian hiện tại ít nhất " + MINIMUM_LEAD_TIME_HOURS + " giờ.");
        }

        // 2. TÌM TẤT CẢ các ca làm việc (WorkingSchedule) của bác sĩ trong ngày đó
        List<WorkingSchedule> schedules = workingScheduleRepository.findByDoctorId(doctor.getId()).stream()
                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                .collect(Collectors.toList());

        if (schedules.isEmpty()) {
            throw new IllegalArgumentException("Bác sĩ " + doctor.getUser().getFullName() + " không có lịch làm việc vào ngày " + dayOfWeek + ".");
        }
        
        // 3. Kiểm tra xem thời gian hẹn có NẰM TRONG BẤT KỲ ca nào không
        boolean isInWorkingHours = schedules.stream().anyMatch(schedule -> {
            // Check nếu giờ bắt đầu của lịch hẹn nằm trong ca làm việc
            boolean startIsIn = !requestedTimeLocal.isBefore(schedule.getStartTime()) && 
                                !requestedTimeLocal.isAfter(schedule.getEndTime());
            
            // Check nếu giờ kết thúc của lịch hẹn (sau duration) nằm trong ca làm việc
            LocalTime endTimePlusDuration = requestedTimeLocal.plusMinutes(duration);
            boolean endIsIn = !endTimePlusDuration.isBefore(schedule.getStartTime()) &&
                              !endTimePlusDuration.isAfter(schedule.getEndTime());
            
            // Slot hợp lệ nếu START nằm trong ca và END (sau duration) cũng nằm trong ca hoặc trùng với EndTime của ca.
            return startIsIn && !endTimePlusDuration.isAfter(schedule.getEndTime());
        });

        if (!isInWorkingHours) {
            String scheduleInfo = schedules.stream()
                .map(s -> String.format("%s - %s", s.getStartTime(), s.getEndTime()))
                .collect(Collectors.joining(", "));
            
            throw new IllegalArgumentException(
                    String.format("Thời gian hẹn không nằm trong bất kỳ ca làm việc nào của bác sĩ vào ngày này. Lịch làm việc: [%s]", scheduleInfo)
            );
        }
        
        // *Không cần return vì logic đã hoàn thành
    }

    // Lấy thông tin một lịch hẹn theo ID và xác thực quyền của bác sĩ.
    public Appointment getAppointmentByIdForDoctor(Long appointmentId) {
        User currentDoctor = authenticationService.getCurrentAuthenticatedUser();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch hẹn."));

        // Xác thực bác sĩ đang đăng nhập chính là bác sĩ của lịch hẹn này
        if (!Objects.equals(appointment.getDoctor().getUser().getId(), currentDoctor.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập lịch hẹn này.");
        }

        return appointment;
    }
}