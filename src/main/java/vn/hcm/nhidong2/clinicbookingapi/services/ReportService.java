package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReportDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment; // THÊM
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty; // THÊM
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.SpecialtyRepository; // THÊM

import java.time.LocalDate; // THÊM
import java.time.LocalTime; // THÊM
import java.time.OffsetDateTime; // THÊM
import java.time.ZoneId; // THÊM
import java.util.Arrays;
import java.util.List;
import java.util.Map; // THÊM
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository; // THÊM

    private static final ZoneId HOSPITAL_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh"); // THÊM

    /**
     * Thống kê số lượng lịch hẹn theo trạng thái. (Đã tối ưu hóa)
     */
    public List<ReportDTO> getAppointmentStatusReport() {
        // Lấy tất cả các lịch hẹn một lần
        List<Appointment> allAppointments = appointmentRepository.findAll();

        // Đếm trong bộ nhớ
        Map<AppointmentStatus, Long> countMap = allAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));

        return Arrays.stream(AppointmentStatus.values())
                .map(status -> ReportDTO.builder()
                        .name(status.name())
                        .count(countMap.getOrDefault(status, 0L))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Thống kê số lượng lịch hẹn đã HOÀN THÀNH cho mỗi bác sĩ (Tải trọng khám bệnh). (Đã tối ưu hóa)
     */
    public List<ReportDTO> getDoctorLoadReport() {
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        // Lấy tất cả lịch hẹn đã hoàn thành
        List<Appointment> completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();
        
        // Đếm trong bộ nhớ
        Map<Long, Long> doctorLoadMap = completedAppointments.stream()
                .collect(Collectors.groupingBy(a -> a.getDoctor().getId(), Collectors.counting()));

        return allDoctors.stream()
                .map(doctor -> ReportDTO.builder()
                        .name(doctor.getUser().getFullName())
                        .count(doctorLoadMap.getOrDefault(doctor.getId(), 0L))
                        .additionalInfo(doctor.getSpecialty().getName())
                        .build())
                .sorted((d1, d2) -> Long.compare(d2.getCount(), d1.getCount()))
                .collect(Collectors.toList());
    }

    /**
     * THÊM MỚI: Thống kê lịch hẹn trong một ngày cụ thể (COMPLETED, CANCELLED)
     */
    public List<ReportDTO> getDailyAppointmentReport(LocalDate date) {
        OffsetDateTime startOfDay = date.atStartOfDay(HOSPITAL_ZONE_ID).toOffsetDateTime();
        OffsetDateTime endOfDay = date.atTime(LocalTime.MAX).atZone(HOSPITAL_ZONE_ID).toOffsetDateTime();

        // Lấy tất cả lịch hẹn trong ngày đó
        List<Appointment> appointmentsOnDay = appointmentRepository.findAllByAppointmentDateTimeBetween(startOfDay, endOfDay);

        // Đếm số lượng cho các trạng thái quan trọng
        long completedCount = appointmentsOnDay.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        long cancelledCount = appointmentsOnDay.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .count();

        long totalCount = appointmentsOnDay.size();
        long totalAmount = appointmentsOnDay.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .mapToLong(a -> a.getAmount() != null ? a.getAmount() : 0L)
                .sum();

        return List.of(
                ReportDTO.builder().name("Đã khám").count(completedCount).build(),
                ReportDTO.builder().name("Đã hủy").count(cancelledCount).build(),
                ReportDTO.builder().name("Tổng cộng").count(totalCount).build(),
                ReportDTO.builder().name("Tổng tiền").count(totalAmount).build()
        );
    }

    /**
     * THÊM MỚI: Thống kê tải trọng theo chuyên khoa (số lịch hẹn đã hoàn thành)
     */
    public List<ReportDTO> getAppointmentsBySpecialtyReport() {
        List<Specialty> allSpecialties = specialtyRepository.findAll();
        
        // Lấy tất cả lịch hẹn đã hoàn thành
        List<Appointment> completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

        // Đếm theo ID chuyên khoa
        Map<Long, Long> specialtyLoadMap = completedAppointments.stream()
                .collect(Collectors.groupingBy(a -> a.getDoctor().getSpecialty().getId(), Collectors.counting()));

        return allSpecialties.stream()
                .map(specialty -> ReportDTO.builder()
                        .name(specialty.getName())
                        .count(specialtyLoadMap.getOrDefault(specialty.getId(), 0L))
                        .build())
                .sorted((s1, s2) -> Long.compare(s2.getCount(), s1.getCount()))
                .collect(Collectors.toList());
    }
}
