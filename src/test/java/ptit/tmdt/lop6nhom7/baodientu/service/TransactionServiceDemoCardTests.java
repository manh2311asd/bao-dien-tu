package ptit.tmdt.lop6nhom7.baodientu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ptit.tmdt.lop6nhom7.baodientu.config.DemoCardPaymentConfig;
import ptit.tmdt.lop6nhom7.baodientu.dto.DemoCardPaymentResponse;
import ptit.tmdt.lop6nhom7.baodientu.entity.Transaction;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;
import ptit.tmdt.lop6nhom7.baodientu.entity.VipPackage;
import ptit.tmdt.lop6nhom7.baodientu.enums.TransactionStatus;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserRole;
import ptit.tmdt.lop6nhom7.baodientu.exception.ForbiddenException;
import ptit.tmdt.lop6nhom7.baodientu.repository.TransactionRepo;
import ptit.tmdt.lop6nhom7.baodientu.repository.UserRepo;
import ptit.tmdt.lop6nhom7.baodientu.repository.VipPackageRepo;

@ExtendWith(MockitoExtension.class)
class TransactionServiceDemoCardTests {

  @Mock private TransactionRepo transactionRepository;
  @Mock private VipPackageRepo vipPackageRepository;
  @Mock private UserRepo userRepository;
  @Mock private VNPayService vnPayService;
  @Mock private DemoCardPaymentConfig demoCardPaymentConfig;
  @InjectMocks private TransactionService transactionService;

  private User user;
  private VipPackage vipPackage;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(7);
    user.setRole(UserRole.MEMBER);

    vipPackage = new VipPackage();
    vipPackage.setId(2);
    vipPackage.setName("VIP 60");
    vipPackage.setDurationDays(60);
    vipPackage.setPrice(new BigDecimal("810000.00"));
    vipPackage.setDiscountPercent(10);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(user.getId(), null, List.of())
    );
    when(demoCardPaymentConfig.isEnabled()).thenReturn(true);
    when(userRepository.findById(7L)).thenReturn(Optional.of(user));
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createDemoPaymentUsesDiscountedServerAmount() {
    when(vipPackageRepository.findById(2)).thenReturn(Optional.of(vipPackage));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction saved = invocation.getArgument(0);
      saved.setId(41);
      return saved;
    });

    DemoCardPaymentResponse result = transactionService.createDemoCardPayment(2);

    assertEquals(0, new BigDecimal("729000.00").compareTo(result.getAmount()));
    assertEquals(TransactionStatus.PENDING, result.getStatus());
    assertEquals(41, result.getTransactionId());
    assertNotNull(result.getPaymentCode());
  }

  @Test
  void staffAccountCannotPurchaseVipPackage() {
    user.setRole(UserRole.ADMIN);

    assertThrows(
        ForbiddenException.class,
        () -> transactionService.createDemoCardPayment(2)
    );
    verify(vipPackageRepository, never()).findById(2);
  }

  @Test
  void repeatedConfirmationReturnsSuccessWithoutExtendingVipAgain() {
    Instant existingExpiry = Instant.now().plusSeconds(86400);
    user.setVipExpiryDate(existingExpiry);
    Transaction transaction = transaction(TransactionStatus.SUCCESS);

    when(transactionRepository.findByIdForUpdate(41)).thenReturn(Optional.of(transaction));
    when(userRepository.findByIdForUpdate(7)).thenReturn(Optional.of(user));

    DemoCardPaymentResponse result = transactionService.confirmDemoCardPayment(
        41,
        transaction.getPaymentCode()
    );

    assertEquals(TransactionStatus.SUCCESS, result.getStatus());
    assertEquals(existingExpiry, result.getVipExpiryDate());
    verify(userRepository, never()).save(any(User.class));
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void pendingConfirmationActivatesVipAndMarksTransactionSuccessful() {
    Transaction transaction = transaction(TransactionStatus.PENDING);
    when(transactionRepository.findByIdForUpdate(41)).thenReturn(Optional.of(transaction));
    when(userRepository.findByIdForUpdate(7)).thenReturn(Optional.of(user));
    when(transactionRepository.save(transaction)).thenReturn(transaction);
    when(userRepository.save(user)).thenReturn(user);

    DemoCardPaymentResponse result = transactionService.confirmDemoCardPayment(
        41,
        transaction.getPaymentCode()
    );

    assertEquals(TransactionStatus.SUCCESS, result.getStatus());
    assertNotNull(result.getVipExpiryDate());
    assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
    verify(userRepository).save(user);
    verify(transactionRepository).save(transaction);
  }

  private Transaction transaction(TransactionStatus status) {
    Transaction transaction = new Transaction();
    transaction.setId(41);
    transaction.setUser(user);
    transaction.setPackageField(vipPackage);
    transaction.setAmount(new BigDecimal("729000.00"));
    transaction.setPaymentCode("DEMO-VISA-abc123");
    transaction.setStatus(status);
    transaction.setCreatedAt(Instant.now());
    return transaction;
  }
}
