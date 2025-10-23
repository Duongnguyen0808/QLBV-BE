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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled; // <-- THÊM DÒNG NÀY

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final AppointmentRepository appointmentRepository;
    private final TransactionRepository transactionRepository;
    private final AuthenticationService authenticationService;

    private static final Long APPOINTMENT_FEE = 10000L; 
    // Giữ nguyên 1 phút để test tính năng tự động hủy 
    private static final long TRANSACTION_EXPIRY_MINUTES = 1; 
    
    // --- CẤU HÌNH VIETQR QUICK LINK ---
    private static final String VIETQR_BASE_URL = "https://img.vietqr.io/image";
    private static final String BANK_ID = "VIETCOMBANK"; 
    private static final String ACCOUNT_NO = "2345745181"; 
    private static final String TEMPLATE = "compact2"; 
    // ------------------------------------

    /**
     * Khởi tạo yêu cầu thanh toán và tạo URL QR code (VietQR Quick Link).
     */
    @Transactional
    public PaymentResponseDTO createPaymentRequest(PaymentRequestDTO requestDTO) {
        User currentUser = authenticationService.getCurrentAuthenticatedUser();
        Appointment appointment = appointmentRepository.findById(requestDTO.getAppointmentId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch hẹn."));
        
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
                // THÊM LOGIC: Nếu giao dịch cũ đã hết hạn, coi như không tồn tại
                .filter(t -> t.getCreatedAt().isAfter(OffsetDateTime.now().minusMinutes(TRANSACTION_EXPIRY_MINUTES))) 
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
        
        // --- TẠO URL QR CODE THEO CÚ PHÁP VIETQR QUICK LINK ---
        String patientFullName = appointment.getPatient().getFullName();
        String description = "TT LK ID " + transaction.getId(); 
        
        String baseUrl = String.format("%s/%s-%s-%s.png", 
                                        VIETQR_BASE_URL, 
                                        BANK_ID, 
                                        ACCOUNT_NO, 
                                        TEMPLATE);
        
        String queryParams;
        try {
            String encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString());
            String encodedAccountName = URLEncoder.encode(patientFullName, StandardCharsets.UTF_8.toString());
            
            queryParams = String.format("amount=%d&addInfo=%s&accountName=%s",
                                        transaction.getAmount(), 
                                        encodedDescription,
                                        encodedAccountName);

        } catch (UnsupportedEncodingException e) {
            log.error("Lỗi mã hóa URL cho QR Code", e);
            queryParams = String.format("amount=%d&addInfo=THANHTOANLICHKHAM", transaction.getAmount());
        }

        String qrCodeUrl = baseUrl + "?" + queryParams;
        // -----------------------------------------------------------------------

        return PaymentResponseDTO.builder()
                .paymentUrl(qrCodeUrl) 
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .build();
    }
    
    /**
     * PHƯƠNG THỨC NÀY ĐÃ ĐƯỢC THÊM ĐỂ KHẮC PHỤC LỖI BIÊN DỊCH
     */
    @Transactional
    public int cancelExpiredPendingTransactions() {
        // Sử dụng hằng số 1 phút
        OffsetDateTime expiryTime = OffsetDateTime.now().minusMinutes(TRANSACTION_EXPIRY_MINUTES);
        List<Transaction> expiredTransactions = transactionRepository.findAll().stream()
            .filter(t -> t.getStatus() == PaymentStatus.PENDING)
            .filter(t -> t.getCreatedAt().isBefore(expiryTime))
            .collect(Collectors.toList());
            
        for (Transaction txn : expiredTransactions) {
            txn.setStatus(PaymentStatus.CANCELLED);
            // Cập nhật trạng thái lịch hẹn liên quan
            Appointment appointment = txn.getAppointment();
            if (appointment.getStatus() == AppointmentStatus.PAID_PENDING) {
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);
            }
            transactionRepository.save(txn);
        }
        log.info("Đã tự động hủy {} giao dịch quá hạn.", expiredTransactions.size());
        return expiredTransactions.size();
    }


    // THÊM: Tác vụ lập lịch để tự động hủy các giao dịch đã hết hạn
    @Scheduled(fixedRateString = "30000") // Chạy mỗi 30 giây (30000ms)
    @Transactional
    public void scheduledCancelExpiredTransactions() {
        cancelExpiredPendingTransactions();
    }


    /**
     * Xử lý Callback/IPN từ cổng thanh toán.
     */
    @Transactional
    public String handlePaymentCallback(Long transactionId, String status, String transactionCode) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy giao dịch."));
        
        // KIỂM TRA HẾT HẠN TRƯỚC KHI XÁC NHẬN
        if (transaction.getStatus() == PaymentStatus.PENDING && 
            // Sử dụng hằng số 1 phút
            transaction.getCreatedAt().isBefore(OffsetDateTime.now().minusMinutes(TRANSACTION_EXPIRY_MINUTES))) {
            transaction.setStatus(PaymentStatus.FAILED);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Giao dịch đã hết hạn, không thể xác nhận.");
        }


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