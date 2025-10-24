package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReportDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.Specialty;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.SpecialtyRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    private static final ZoneId HOSPITAL_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Thống kê số lượng lịch hẹn theo trạng thái.
     */
    public List<ReportDTO> getAppointmentStatusReport() {
        List<AppointmentStatus> allStatuses = Arrays.asList(AppointmentStatus.values());
        return allStatuses.stream()
                .map(status -> {
                    long count = appointmentRepository.findAll().stream()
                            .filter(appointment -> appointment.getStatus() == status)
                            .count();
                    return ReportDTO.builder()
                            .name(status.name())
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Thống kê số lượng lịch hẹn đã HOÀN THÀNH cho mỗi bác sĩ (Tải trọng khám bệnh).
     */
    public List<ReportDTO> getDoctorLoadReport() {
        List<Doctor> allDoctors = doctorRepository.findAll();

        return allDoctors.stream()
                .map(doctor -> {
                    // Dùng AppointmentRepository để tìm và đếm số lịch hẹn đã hoàn thành của từng bác sĩ
                    long finalCount = appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctor.getId())
                            .stream()
                            .filter(appointment -> appointment.getStatus() == AppointmentStatus.COMPLETED)
                            .count();

                    return ReportDTO.builder()
                            .name(doctor.getUser().getFullName())
                            .count(finalCount)
                            .additionalInfo(doctor.getSpecialty().getName())
                            .build();
                })
                .sorted((d1, d2) -> Long.compare(d2.getCount(), d1.getCount()))
                .collect(Collectors.toList());
    }

    /**
     * Thống kê lịch hẹn trong một ngày cụ thể (COMPLETED, CANCELLED)
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

        return List.of(
                ReportDTO.builder().name("Đã Khám (COMPLETED)").count(completedCount).build(),
                ReportDTO.builder().name("Đã Hủy (CANCELLED)").count(cancelledCount).build(),
                ReportDTO.builder().name("Tổng cộng").count(totalCount).build()
        );
    }

    /**
     * Thống kê tải trọng theo chuyên khoa (số lịch hẹn đã hoàn thành)
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