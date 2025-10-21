package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.hcm.nhidong2.clinicbookingapi.dtos.PaymentRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Appointment;
import vn.hcm.nhidong2.clinicbookingapi.models.Transaction;
import vn.hcm.nhidong2.clinicbookingapi.repositories.AppointmentRepository;
import vn.hcm.nhidong2.clinicbookingapi.repositories.TransactionRepository;
import vn.hcm.nhidong2.clinicbookingapi.services.PaymentService;

@Controller
@RequestMapping("/patient/payments")
@RequiredArgsConstructor
public class PatientPaymentWebController {
    
    // Lưu ý: Trong ứng dụng thực tế, cần phải xác thực người dùng đang đăng nhập là chủ sở hữu của lịch hẹn/giao dịch.
    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;

    // Hiển thị trang chờ người dùng quét QR và polling trạng thái
    @GetMapping("/wait/{transactionId}")
    public String showPaymentWaitingPage(@PathVariable Long transactionId, Model model) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
        
        Appointment appointment = appointmentRepository.findById(transaction.getAppointment().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn."));

        model.addAttribute("transaction", transaction);
        model.addAttribute("appointment", appointment);
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("contentView", "patient/payment-waiting");
        
        // Gọi lại Service để lấy URL QR Code với thông tin đã được điền sẵn
        model.addAttribute("quicklinkUrl", paymentService.createPaymentRequest(
                new PaymentRequestDTO(appointment.getId(), transaction.getPaymentMethod())
        ).getPaymentUrl());

        return "fragments/layout";
    }

    @GetMapping("/result/{transactionId}")
    public String showPaymentResultPage(@PathVariable Long transactionId, Model model) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("contentView", "patient/payment-result");
        return "fragments/layout";
    }
}