package ptit.tmdt.lop6nhom7.baodientu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ptit.tmdt.lop6nhom7.baodientu.enums.ArticleType;
import ptit.tmdt.lop6nhom7.baodientu.repository.ArticleViewRepo;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class MonthlyVipReadQuotaService {
  public static final int MONTHLY_FREE_VIP_ARTICLE_LIMIT = 3;
  private static final ZoneId READER_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final ArticleViewRepo articleViewRepo;

  public QuotaStatus inspect(Integer userId, Integer articleId) {
    Instant now = Instant.now();
    Instant startOfMonth = YearMonth.now(READER_ZONE)
        .atDay(1)
        .atStartOfDay(READER_ZONE)
        .toInstant();
    long usedReads = articleViewRepo.countDistinctArticlesByUserAndTypeWithinPeriod(
        userId,
        ArticleType.VIP,
        startOfMonth,
        now
    );
    int remaining = Math.max(0, MONTHLY_FREE_VIP_ARTICLE_LIMIT - (int) usedReads);
    // An opened article stays available to that account. The monthly allowance
    // is only spent on three new VIP articles, never on reopening old ones.
    boolean alreadyRead = articleId != null
        && articleViewRepo.existsByUserIdAndArticleId(userId, articleId);

    return new QuotaStatus(alreadyRead, remaining);
  }

  public int getRemainingReads(Integer userId) {
    return inspect(userId, null).remainingReads();
  }

  public record QuotaStatus(boolean alreadyRead, int remainingReads) {
    public boolean canOpen() {
      return alreadyRead || remainingReads > 0;
    }

    public int remainingAfterOpening() {
      return alreadyRead ? remainingReads : Math.max(0, remainingReads - 1);
    }
  }
}
