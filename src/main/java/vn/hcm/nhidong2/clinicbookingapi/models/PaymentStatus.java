package vn.hcm.nhidong2.clinicbookingapi.models;

public enum PaymentStatus {
    PENDING, // Đang chờ thanh toán (chưa nhận được xác nhận từ cổng TT)
    SUCCESS, // Thanh toán thành công
    FAILED, // Thanh toán thất bại
    REFUNDED, // Đã hoàn tiền
    CANCELLED // Giao dịch bị hủy bỏ
}