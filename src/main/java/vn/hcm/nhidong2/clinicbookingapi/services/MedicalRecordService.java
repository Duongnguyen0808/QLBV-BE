package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.MedicalRecordRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.*;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.MedicalRecordRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuthenticationService authenticationService;
    private final DoctorRepository doctorRepository;

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecordRequestDTO requestDTO) {
        User currentDoctor = authenticationService.getCurrentAuthenticatedUser();

        Appointment appointment = appointmentRepository.findById(requestDTO.getAppointmentId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch hẹn."));

        if (!Objects.equals(appointment.getDoctor().getUser().getId(), currentDoctor.getId() )) {
            throw new AccessDeniedException("Bạn không có quyền tạo bệnh án cho lịch hẹn này.");
        }

        if (medicalRecordRepository.existsByAppointmentId(appointment.getId())) {
            throw new IllegalStateException("Lịch hẹn này đã có bệnh án.");
        }

        MedicalRecord medicalRecord= MedicalRecord.builder()
                .appointment(appointment)
                .diagnosis(requestDTO.getDiagnosis())
                .symptoms(requestDTO.getSymptoms())
                .vitalSigns(requestDTO.getVitalSigns())
                .testResults(requestDTO.getTestResults())
                .prescription(requestDTO.getPrescription())
                .notes(requestDTO.getNotes())
                .reexaminationDate(requestDTO.getReexaminationDate()) // THÊM DÒNG NÀY
                .build();

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getMyMedicalRecords() {
        User currentPatient = authenticationService.getCurrentAuthenticatedUser();
        return medicalRecordRepository.findByAppointment_Patient_IdOrderByAppointment_AppointmentDateTimeDesc(currentPatient.getId());
    }

    public List<MedicalRecord> getMedicalRecordsForPatientByDoctor(Long patientId) {
        User currentDoctorUser = authenticationService.getCurrentAuthenticatedUser();
        Doctor doctorProfile = doctorRepository.findByUser(currentDoctorUser)
                .orElseThrow(() -> new AccessDeniedException("Chỉ bác sĩ mới có quyền truy cập chức năng này."));

        boolean hasAppointmentWithPatient = appointmentRepository.findDistinctPatientsByDoctorId(doctorProfile.getId())
                .stream().anyMatch(patient -> patient.getId().equals(patientId));

        if (!hasAppointmentWithPatient) {
            throw new AccessDeniedException("Bạn không có quyền xem lịch sử khám bệnh của bệnh nhân này.");
        }

        return medicalRecordRepository.findByAppointment_Patient_IdOrderByAppointment_AppointmentDateTimeDesc(patientId);
    }
}