package vn.hcm.nhidong2.clinicbookingapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hcm.nhidong2.clinicbookingapi.models.Transaction;
import vn.hcm.nhidong2.clinicbookingapi.models.PaymentStatus; 

import java.util.List; 
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByAppointmentId(Long appointmentId);
    Optional<Transaction> findByTransactionCode(String transactionCode);
    
    // THÊM: Tìm kiếm các giao dịch theo trạng thái
    List<Transaction> findByStatus(PaymentStatus status);
}