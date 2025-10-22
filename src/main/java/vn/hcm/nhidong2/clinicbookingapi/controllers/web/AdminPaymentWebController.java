package vn.hcm.nhidong2.clinicbookingapi.controllers.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hcm.nhidong2.clinicbookingapi.models.PaymentStatus;
import vn.hcm.nhidong2.clinicbookingapi.models.Transaction;
import vn.hcm.nhidong2.clinicbookingapi.repositories.TransactionRepository;
import vn.hcm.nhidong2.clinicbookingapi.services.PaymentService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentWebController {

    private final TransactionRepository transactionRepository;
    private final PaymentService paymentService;

    // SỬA: Hiển thị danh sách các giao dịch đang chờ VÀ đã thành công
    @GetMapping
    public String showPendingTransactions(Model model) {
        // Lấy tất cả các giao dịch (Bạn có thể giới hạn số lượng trong DB nếu cần)
        List<Transaction> allTransactions = transactionRepository.findAll();

        // Lọc chỉ giữ lại PENDING và SUCCESS (hoặc chỉ PENDING và SUCCESS hôm nay nếu bạn muốn ngắn gọn)
        List<Transaction> filteredTransactions = allTransactions.stream()
            .filter(t -> t.getStatus() == PaymentStatus.PENDING || t.getStatus() == PaymentStatus.SUCCESS)
            .collect(Collectors.toList());
            
        model.addAttribute("transactions", filteredTransactions);
        model.addAttribute("contentView", "admin/transactions-list");
        
        // THÊM: Chuẩn bị danh sách các giao dịch đang chờ để gửi cho chức năng tra soát mô phỏng
        long pendingCount = filteredTransactions.stream()
            .filter(t -> t.getStatus() == PaymentStatus.PENDING)
            .count();
        model.addAttribute("pendingCount", pendingCount);
        
        return "fragments/layout";
    }

    // Xử lý khi Admin nhấn nút "Xác nhận Thanh toán" (Mô phỏng Callback thành công)
    @PostMapping("/{transactionId}/confirm-success")
    public String confirmTransactionSuccess(@PathVariable Long transactionId, RedirectAttributes ra) {
        try {
            // TẠO MÃ GIAO DỊCH GIẢ ĐỊNH
            String simulatedTxnCode = "ADMIN_CONFIRM_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Gọi service xử lý Callback với trạng thái SUCCESS
            paymentService.handlePaymentCallback(transactionId, "SUCCESS", simulatedTxnCode);
            
            ra.addFlashAttribute("successMessage", "Đã xác nhận thanh toán thành công cho Giao dịch #" + transactionId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi xác nhận giao dịch: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }
    
    /**
     * THÊM: Mô phỏng Tra soát Ngân hàng (Scanning Bank Transactions)
     */
    @PostMapping("/scan-bank-transactions")
    public String reviewBankTransactions(RedirectAttributes ra) {
        // BƯỚC 1: Hủy các giao dịch đã hết hạn (giả định bệnh nhân đã bỏ qua)
        // LỖI BIÊN DỊCH Ở ĐÂY ĐÃ ĐƯỢC KHẮC PHỤC KHI CÓ PHƯƠNG THỨC MỚI
        int cancelledCount = paymentService.cancelExpiredPendingTransactions(); 
        
        // Lấy lại danh sách PENDING còn hợp lệ
        List<Transaction> pendingTransactions = transactionRepository.findByStatus(PaymentStatus.PENDING);
        int successfulCount = 0;
        
        // BƯỚC 2: Mô phỏng Tra soát và Xác nhận
        for (Transaction txn : pendingTransactions) {
            try {
                // MÔ PHỎNG: Giả sử giao dịch này đã thành công trên hệ thống ngân hàng
                String simulatedTxnCode = "BANK_SCAN_" + txn.getId();
                paymentService.handlePaymentCallback(txn.getId(), "SUCCESS", simulatedTxnCode);
                successfulCount++;
            } catch (Exception e) {
                // Bỏ qua lỗi và tiếp tục giao dịch tiếp theo
            }
        }
        
        if (successfulCount > 0) {
            ra.addFlashAttribute("successMessage", 
                String.format("Đã hủy %d giao dịch quá hạn. Đã tra soát thành công và tự động xác nhận %d/%d giao dịch còn lại.", 
                              cancelledCount, successfulCount, pendingTransactions.size()));
        } else {
             ra.addFlashAttribute("errorMessage", String.format("Đã hủy %d giao dịch quá hạn. Không tìm thấy giao dịch chờ nào để tra soát/xác nhận tự động.", cancelledCount));
        }
        
        return "redirect:/admin/payments";
    }
}