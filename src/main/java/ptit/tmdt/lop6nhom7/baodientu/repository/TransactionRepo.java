package ptit.tmdt.lop6nhom7.baodientu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ptit.tmdt.lop6nhom7.baodientu.entity.Transaction;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.id = :id")
    Optional<Transaction> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.paymentCode = :paymentCode")
    Optional<Transaction> findByPaymentCodeForUpdate(@Param("paymentCode") String paymentCode);

    /** Lấy lịch sử giao dịch của một user, mới nhất trước. */
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}
