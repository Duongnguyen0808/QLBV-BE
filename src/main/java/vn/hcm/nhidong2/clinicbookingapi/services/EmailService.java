package vn.hcm.nhidong2.clinicbookingapi.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Async
    public void sendVerificationEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setText(htmlBody, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(mailFrom); // dùng tài khoản đã cấu hình để tránh bị chặn

            mailSender.send(mimeMessage);

            log.info("HTML email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            throw new IllegalStateException("Gửi email thất bại", e);
        }
    }

    @Async
    public void sendAppointmentStatusEmail(String to, String subject, String patientName, String doctorName, String appointmentTime, String status) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlBody = String.format("""
                <h1>Thông báo Lịch hẹn tại Bệnh viện Nhi Đồng II</h1>
                <p>Xin chào %s,</p>
                <p>Lịch hẹn của bạn đã được cập nhật với thông tin chi tiết như sau:</p>
                <ul>
                    <li><strong>Bác sĩ:</strong> %s</li>
                    <li><strong>Thời gian:</strong> %s</li>
                    <li><strong>Trạng thái mới:</strong> <strong style="color: green;">%s</strong></li>
                </ul>
                <p>Vui lòng đăng nhập vào hệ thống để xem chi tiết. Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.</p>
                """, patientName, doctorName, appointmentTime, status);

            helper.setText(htmlBody, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(mailFrom); // dùng tài khoản SMTP cấu hình

            mailSender.send(mimeMessage);

            log.info("Appointment status email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send appointment status email", e);
            throw new IllegalStateException("Gửi email thất bại", e);
        }
    }
}
