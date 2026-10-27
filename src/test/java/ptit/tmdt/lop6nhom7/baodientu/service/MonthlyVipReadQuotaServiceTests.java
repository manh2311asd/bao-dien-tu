package ptit.tmdt.lop6nhom7.baodientu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ptit.tmdt.lop6nhom7.baodientu.enums.ArticleType;
import ptit.tmdt.lop6nhom7.baodientu.repository.ArticleViewRepo;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyVipReadQuotaServiceTests {
  @Mock
  private ArticleViewRepo articleViewRepo;

  @InjectMocks
  private MonthlyVipReadQuotaService quotaService;

  @Test
  void newVipArticleConsumesOneOfThreeMonthlyReads() {
    when(articleViewRepo.countDistinctArticlesByUserAndTypeWithinPeriod(
        eq(12), eq(ArticleType.VIP), any(Instant.class), any(Instant.class)
    )).thenReturn(1L);
    when(articleViewRepo.existsByUserIdAndArticleId(12, 2051)).thenReturn(false);

    MonthlyVipReadQuotaService.QuotaStatus quota = quotaService.inspect(12, 2051);

    assertFalse(quota.alreadyRead());
    assertTrue(quota.canOpen());
    assertEquals(2, quota.remainingReads());
    assertEquals(1, quota.remainingAfterOpening());
  }

  @Test
  void reopeningPreviouslyUnlockedArticleDoesNotConsumeAgain() {
    when(articleViewRepo.countDistinctArticlesByUserAndTypeWithinPeriod(
        eq(12), eq(ArticleType.VIP), any(Instant.class), any(Instant.class)
    )).thenReturn(1L);
    when(articleViewRepo.existsByUserIdAndArticleId(12, 2051)).thenReturn(true);

    MonthlyVipReadQuotaService.QuotaStatus quota = quotaService.inspect(12, 2051);

    assertTrue(quota.alreadyRead());
    assertTrue(quota.canOpen());
    assertEquals(2, quota.remainingAfterOpening());
  }

  @Test
  void fourthDistinctVipArticleIsBlocked() {
    when(articleViewRepo.countDistinctArticlesByUserAndTypeWithinPeriod(
        eq(12), eq(ArticleType.VIP), any(Instant.class), any(Instant.class)
    )).thenReturn(3L);
    when(articleViewRepo.existsByUserIdAndArticleId(12, 2051)).thenReturn(false);

    MonthlyVipReadQuotaService.QuotaStatus quota = quotaService.inspect(12, 2051);

    assertFalse(quota.canOpen());
    assertEquals(0, quota.remainingReads());
  }
}
