package ptit.tmdt.lop6nhom7.baodientu.rss;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class NewsAggregatorService {
    static final Duration CACHE_TTL = Duration.ofMinutes(3);
    static final Duration MIN_FORCED_REFRESH_INTERVAL = Duration.ofSeconds(30);

    private final RssSourceProperties properties;
    private final ExternalNewsFeedClient feedClient;
    private final Clock clock;
    private final ReentrantLock refreshLock = new ReentrantLock();

    private volatile Snapshot snapshot = new Snapshot(List.of(), Instant.EPOCH);

    @Autowired
    public NewsAggregatorService(
            RssSourceProperties properties,
            ExternalNewsFeedClient feedClient) {
        this(properties, feedClient, Clock.systemUTC());
    }

    NewsAggregatorService(
            RssSourceProperties properties,
            ExternalNewsFeedClient feedClient,
            Clock clock) {
        this.properties = properties;
        this.feedClient = feedClient;
        this.clock = clock;
    }

    public ExternalNewsPageResponse getNews(
            String category,
            String source,
            String keyword,
            int requestedPage,
            int requestedPageSize,
            boolean forceRefresh) {
        Snapshot current = getSnapshot(forceRefresh);
        String categoryFilter = normalizeCategory(category);
        String sourceFilter = normalize(source);
        String keywordFilter = normalize(keyword);

        List<ExternalNewsDto> filtered = current.items().stream()
                .filter(item -> categoryFilter.isBlank()
                        || normalize(item.category()).equals(categoryFilter))
                .filter(item -> sourceFilter.isBlank()
                        || normalize(item.source()).equals(sourceFilter))
                .filter(item -> keywordFilter.isBlank()
                        || normalize(item.title()).contains(keywordFilter)
                        || normalize(item.description()).contains(keywordFilter))
                .toList();

        int page = Math.max(1, requestedPage);
        int pageSize = Math.min(50, Math.max(1, requestedPageSize));
        int fromIndex = Math.min((page - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());

        return new ExternalNewsPageResponse(
                current.updatedAt(),
                page,
                pageSize,
                filtered.size(),
                toIndex < filtered.size(),
                filtered.subList(fromIndex, toIndex));
    }

    public void refreshCache() {
        getSnapshot(true);
    }

    private Snapshot getSnapshot(boolean forceRefresh) {
        Instant now = clock.instant();
        Snapshot current = snapshot;
        boolean expired = isExpired(current, now);
        boolean forcedRefreshAllowed = forceRefresh
                && Duration.between(current.updatedAt(), now)
                        .compareTo(MIN_FORCED_REFRESH_INTERVAL) >= 0;

        if (!expired && !forcedRefreshAllowed) {
            log.info("News cache HIT: {} articles", current.items().size());
            return current;
        }

        log.info("News cache MISS: refreshRequested={}, expired={}", forceRefresh, expired);
        refreshLock.lock();
        try {
            current = snapshot;
            now = clock.instant();
            expired = isExpired(current, now);
            forcedRefreshAllowed = forceRefresh
                    && Duration.between(current.updatedAt(), now)
                            .compareTo(MIN_FORCED_REFRESH_INTERVAL) >= 0;
            if (!expired && !forcedRefreshAllowed) {
                log.info("News cache HIT after waiting for another refresh");
                return current;
            }

            List<RssSourceProperties.FeedSource> sources = properties.getSources();
            if (!properties.isEnabled() || sources == null || sources.isEmpty()) {
                return current;
            }

            List<ExternalNewsDto> fetched = sources.parallelStream()
                    .flatMap(feed -> fetchSafely(feed).stream())
                    .toList();
            Map<String, ExternalNewsDto> unique = new LinkedHashMap<>();
            fetched.stream()
                    .sorted(Comparator.comparing(ExternalNewsDto::publishedAt).reversed())
                    .forEach(item -> unique.putIfAbsent(item.articleUrl(), item));

            if (unique.isEmpty()) {
                log.warn("News aggregation returned no articles; keeping previous cache");
                return current;
            }

            Snapshot refreshed = new Snapshot(List.copyOf(unique.values()), clock.instant());
            snapshot = refreshed;
            log.info("News aggregated: {} articles", refreshed.items().size());
            return refreshed;
        } finally {
            refreshLock.unlock();
        }
    }

    private List<ExternalNewsDto> fetchSafely(RssSourceProperties.FeedSource source) {
        try {
            log.info("Fetching external news: {}", source.getName());
            return feedClient.fetch(source);
        } catch (Exception exception) {
            log.warn("RSS source failed: {} ({})", source.getName(), exception.getMessage());
            return List.of();
        }
    }

    private boolean isExpired(Snapshot value, Instant now) {
        return value.items().isEmpty()
                || Duration.between(value.updatedAt(), now).compareTo(CACHE_TTL) >= 0;
    }

    private String normalizeCategory(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "technology" -> "công nghệ";
            case "business" -> "kinh doanh";
            case "latest" -> "tin mới nhất";
            default -> normalized;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record Snapshot(List<ExternalNewsDto> items, Instant updatedAt) {
    }
}
