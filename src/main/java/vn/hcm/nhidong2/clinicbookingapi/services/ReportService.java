package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReportDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

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
}