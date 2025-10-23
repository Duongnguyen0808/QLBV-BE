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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// THÊM CÁC IMPORTS CỦA ITEXT 
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuthenticationService authenticationService;
    private final DoctorRepository doctorRepository;

    // SỬA LỖI: SỬ DỤNG findByAppointment_Id ĐÃ KHAI BÁO TRONG REPOSITORY
    public Optional<MedicalRecord> getMedicalRecordByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.findByAppointment_Id(appointmentId);
    }
    
    // HÀM NÀY ĐÃ BỊ LỖI Ở CODE CỦA BẠN TRƯỚC ĐÓ, TÔI GIỮ LẠI ĐỊNH NGHĨA NHƯ TRONG FILE CỦA BẠN.
    // public MedicalRecord getMedicalRecordByAppointmentId(Long appointmentId) {
    //     return medicalRecordRepository.findByAppointment_Id(appointmentId)
    //         .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh án cho lịch hẹn này."));
    // }


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

    // HÀM MỚI: TẠO PDF CHO HỒ SƠ BỆNH ÁN
    public byte[] generateMedicalRecordPdf(Long appointmentId) throws DocumentException, IOException {
        MedicalRecord record = medicalRecordRepository.findByAppointment_Id(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh án để xuất PDF."));

        // Xác thực quyền (Bác sĩ phải là người phụ trách lịch hẹn này)
        User currentDoctor = authenticationService.getCurrentAuthenticatedUser();
        if (!Objects.equals(record.getAppointment().getDoctor().getUser().getId(), currentDoctor.getId() )) {
            throw new AccessDeniedException("Bạn không có quyền xuất PDF cho bệnh án này.");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();
        
        // Cần sử dụng font hỗ trợ Tiếng Việt (Thường là Unicode)
        // Lưu ý: Tên font/đường dẫn font có thể cần được điều chỉnh tùy thuộc vào môi trường chạy
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 16, Font.BOLD, BaseColor.BLUE);
        Font headerFont = new Font(bf, 12, Font.BOLD);
        Font normalFont = new Font(bf, 12, Font.NORMAL);
        Font smallFont = new Font(bf, 10, Font.ITALIC);


        // --- TIÊU ĐỀ ---
        Paragraph title = new Paragraph("HỒ SƠ BỆNH ÁN KHÁM NGOẠI TRÚ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        // --- THÔNG TIN CHUNG ---
        document.add(new Paragraph("Bệnh viện Nhi Đồng II", smallFont));
        document.add(new Paragraph("____________________________________________", smallFont));
        document.add(new Paragraph("Bác sĩ phụ trách: " + record.getAppointment().getDoctor().getUser().getFullName(), normalFont));
        document.add(new Paragraph("Chuyên khoa: " + record.getAppointment().getDoctor().getSpecialty().getName(), normalFont));
        document.add(new Paragraph("Bệnh nhân: " + record.getAppointment().getPatient().getFullName(), normalFont));
        document.add(new Paragraph("Ngày khám: " + record.getAppointment().getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")), normalFont));
        document.add(new Paragraph("\n"));
        
        // --- NỘI DUNG BỆNH ÁN ---
        
        // 1. Triệu chứng
        document.add(new Paragraph("1. Triệu chứng:", headerFont));
        document.add(new Paragraph(record.getSymptoms(), normalFont));
        document.add(new Paragraph("\n"));
        
        // 2. Dấu hiệu sinh tồn
        document.add(new Paragraph("2. Dấu hiệu sinh tồn:", headerFont));
        document.add(new Paragraph(record.getVitalSigns(), normalFont));
        document.add(new Paragraph("\n"));
        
        // 3. Chẩn đoán
        document.add(new Paragraph("3. Chẩn đoán:", headerFont));
        document.add(new Paragraph(record.getDiagnosis(), normalFont));
        document.add(new Paragraph("\n"));
        
        // 4. Kết quả xét nghiệm
        document.add(new Paragraph("4. Kết quả xét nghiệm:", headerFont));
        document.add(new Paragraph(record.getTestResults(), normalFont));
        document.add(new Paragraph("\n"));
        
        // 5. Đơn thuốc
        document.add(new Paragraph("5. Đơn thuốc (Chỉ định điều trị):", headerFont));
        document.add(new Paragraph(record.getPrescription(), normalFont));
        document.add(new Paragraph("\n"));
        
        // 6. Ghi chú và Tái khám
        document.add(new Paragraph("6. Ghi chú và Kế hoạch:", headerFont));
        document.add(new Paragraph(record.getNotes(), normalFont));
        if (record.getReexaminationDate() != null) {
            document.add(new Paragraph("Ngày tái khám gợi ý: " + record.getReexaminationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
        } else {
            document.add(new Paragraph("Không có ngày tái khám gợi ý.", normalFont));
        }
        
        document.close();
        
        return baos.toByteArray();
    }
}