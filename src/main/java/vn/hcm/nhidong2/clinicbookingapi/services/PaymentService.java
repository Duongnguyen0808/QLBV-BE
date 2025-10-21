package vn.hcm.nhidong2.clinicbookingapi.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PaymentRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PaymentResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.AppointmentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.PaymentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.Transaction;
import vn.hcm.nhidong2.clinicbookingapi.models.User;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.TransactionRepository;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final AppointmentRepository appointmentRepository;
    private final TransactionRepository transactionRepository;
    private final AuthenticationService authenticationService;

    private static final Long APPOINTMENT_FEE = 150000L; // Phí khám mặc định (giả lập)
    
    // MÔ PHỎNG: Endpoint tạo QR code và Account ID
    private static final String QUICKLINK_QR_API_BASE = "https://quicklink.vn/api/qr/generate";
    private static final String HOSPITAL_ACCOUNT_ID = "NHIDONG2_HOSPITAL";

    /**
     * Khởi tạo yêu cầu thanh toán và tạo URL QR code (Quicklink).
     */
    @Transactional
    public PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        Appointment appointment = appointmentRepository.findById(requestDTO.getAppointmentId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch hẹn."));
        
        // Xác thực người dùng đang đăng nhập là bệnh nhân của lịch hẹn
        if (!Objects.equals(appointment.getPatient().getId(), currentUser.getId())) {
             throw new AccessDeniedException("Bạn không có quyền thanh toán cho lịch hẹn này.");
        }

        if (appointment.getAmount() == null || appointment.getAmount() <= 0) {
            appointment.setAmount(APPOINTMENT_FEE);
        }

        if (appointment.getStatus() != AppointmentStatus.PAID_PENDING) {
            throw new IllegalStateException("Lịch hẹn không ở trạng thái chờ thanh toán.");
        }

        // Kiểm tra xem đã có giao dịch PENDING cho lịch hẹn này chưa
        Transaction existingTransaction = transactionRepository.findByAppointmentId(appointment.getId())
                .filter(t -> t.getStatus() == PaymentStatus.PENDING)
                .orElse(null);

        Transaction transaction;
        if (existingTransaction != null) {
            transaction = existingTransaction;
        } else {
            // Tạo bản ghi giao dịch (Transaction) mới
            transaction = Transaction.builder()
                    .appointment(appointment)
                    .amount(appointment.getAmount())
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(requestDTO.getPaymentMethod())
                    .build();
            transactionRepository.save(transaction);
        }
        
        // --- TẠO URL QR CODE (Quicklink) VỚI SỐ TIỀN VÀ MÃ GIAO DỊCH TỰ ĐỘNG ---
        // Sử dụng transaction.getId() làm orderId để cổng TT gọi lại chính xác
        String qrCodeUrl = String.format(
                "%s?account=%s&amount=%d&orderId=%d&description=ThanhToanLichKham_ND2_%d&patientName=%s",
                QUICKLINK_QR_API_BASE,
                HOSPITAL_ACCOUNT_ID,
                transaction.getAmount(), // Số tiền (amount) tự động nhảy vào QR
                transaction.getId(),     // Mã giao dịch nội bộ
                appointment.getId(),
                appointment.getPatient().getFullName()
        );
        // -----------------------------------------------------------------------

        return PaymentResponseDTO.builder()
                .paymentUrl(qrCodeUrl) // paymentUrl chứa link QR code
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .build();
    }

    /**
     * Xử lý Callback/IPN từ cổng thanh toán.
     */
    @Transactional
    public String handlePaymentCallback(Long transactionId, String status, String transactionCode) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy giao dịch."));

        // Nếu giao dịch đã thành công trước đó thì bỏ qua
        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return "Giao dịch đã được ghi nhận thành công trước đó.";
        }

        // Giả lập xác thực thành công và xử lý kết quả
        if (status.equalsIgnoreCase("SUCCESS")) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setTransactionCode(transactionCode);
            transactionRepository.save(transaction);

            // Cập nhật trạng thái lịch hẹn
            Appointment appointment = transaction.getAppointment();
            if (appointment.getStatus() == AppointmentStatus.PAID_PENDING) {
                appointment.setStatus(AppointmentStatus.CONFIRMED); // Chuyển sang trạng thái đã xác nhận
                appointmentRepository.save(appointment);
                log.info("Thanh toán thành công và xác nhận lịch hẹn: {}", appointment.getId());
            }

            return "Thanh toán thành công. Lịch hẹn đã được xác nhận.";
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setTransactionCode(transactionCode != null ? transactionCode : UUID.randomUUID().toString());
            transactionRepository.save(transaction);
            log.warn("Thanh toán thất bại cho giao dịch: {}", transactionId);
            return "Thanh toán thất bại. Vui lòng thử lại.";
        }
    }
    
    /**
     * Kiểm tra trạng thái giao dịch (dùng cho Web Polling).
     */
    public PaymentStatus getTransactionStatus(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
        return transaction.getStatus();
    }
}