package ptit.tmdt.lop6nhom7.baodientu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ptit.tmdt.lop6nhom7.baodientu.entity.Transaction;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;

import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {

    /** Tìm giao dịch theo mã vnp_TxnRef để xử lý callback. */
    Optional<Transaction> findByPaymentCode(String paymentCode);

    /** Lấy lịch sử giao dịch của một user, mới nhất trước. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"packageField"})
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}