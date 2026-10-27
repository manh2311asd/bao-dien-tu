package ptit.tmdt.lop6nhom7.baodientu.rss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsAggregatorServiceTests {
    @Mock
    private ExternalNewsFeedClient feedClient;

    private RssSourceProperties.FeedSource source;
    private MutableClock clock;
    private NewsAggregatorService service;

    @BeforeEach
    void setUp() {
        source = new RssSourceProperties.FeedSource();
        source.setName("VnExpress");
        source.setUrl("https://example.test/rss");
        source.setDefaultCategory("Công nghệ");

        RssSourceProperties properties = new RssSourceProperties();
        properties.setEnabled(true);
        properties.setSources(List.of(source));
        clock = new MutableClock(Instant.parse("2026-08-15T10:00:00Z"));
        service = new NewsAggregatorService(properties, feedClient, clock);
    }

    @Test
    void reusesCacheUntilThreeMinuteTtlExpires() throws Exception {
        when(feedClient.fetch(source)).thenReturn(List.of(article("id-1", "https://example.test/1")));

        ExternalNewsPageResponse first = service.getNews(null, null, null, 1, 20, false);
        ExternalNewsPageResponse second = service.getNews(null, null, null, 1, 20, false);

        assertThat(first.items()).hasSize(1);
        assertThat(second.updatedAt()).isEqualTo(first.updatedAt());
        verify(feedClient, times(1)).fetch(source);

        clock.advance(NewsAggregatorService.CACHE_TTL.plusSeconds(1));
        service.getNews(null, null, null, 1, 20, false);
        verify(feedClient, times(2)).fetch(source);
    }

    @Test
    void throttlesForcedRefreshAndKeepsDataWhenSourceFails() throws Exception {
        when(feedClient.fetch(source))
                .thenReturn(List.of(article("id-1", "https://example.test/1")))
                .thenThrow(new IllegalStateException("timeout"));

        ExternalNewsPageResponse first = service.getNews(null, null, null, 1, 20, false);
        service.getNews(null, null, null, 1, 20, true);
        verify(feedClient, times(1)).fetch(source);

        clock.advance(NewsAggregatorService.MIN_FORCED_REFRESH_INTERVAL.plusSeconds(1));
        ExternalNewsPageResponse afterFailure = service.getNews(null, null, null, 1, 20, true);

        assertThat(afterFailure.items()).containsExactlyElementsOf(first.items());
        verify(feedClient, times(2)).fetch(source);
    }

    @Test
    void returnsHealthySourcesWhenOneFeedFails() throws Exception {
        RssSourceProperties.FeedSource failingSource = new RssSourceProperties.FeedSource();
        failingSource.setName("Nguồn lỗi");
        failingSource.setUrl("https://example.test/broken");
        failingSource.setDefaultCategory("Công nghệ");

        RssSourceProperties properties = new RssSourceProperties();
        properties.setEnabled(true);
        properties.setSources(List.of(source, failingSource));
        service = new NewsAggregatorService(properties, feedClient, clock);
        when(feedClient.fetch(source)).thenReturn(List.of(article("id-1", "https://example.test/1")));
        when(feedClient.fetch(failingSource)).thenThrow(new IllegalStateException("timeout"));

        ExternalNewsPageResponse response = service.getNews(null, null, null, 1, 20, false);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).source()).isEqualTo("VnExpress");
    }

    @Test
    void concurrentCacheMissesTriggerOnlyOneFeedFetch() throws Exception {
        when(feedClient.fetch(source)).thenAnswer(invocation -> {
            Thread.sleep(75);
            return List.of(article("id-1", "https://example.test/1"));
        });
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<ExternalNewsPageResponse>> requests = java.util.stream.IntStream.range(0, 8)
                    .mapToObj(index -> executor.submit(
                            () -> service.getNews(null, null, null, 1, 20, false)))
                    .toList();

            for (Future<ExternalNewsPageResponse> request : requests) {
                assertThat(request.get().items()).hasSize(1);
            }
            verify(feedClient, times(1)).fetch(source);
        } finally {
            executor.shutdownNow();
        }
    }

    private ExternalNewsDto article(String id, String url) {
        return new ExternalNewsDto(
                id,
                "Tin mới",
                "Mô tả",
                "https://example.test/image.jpg",
                url,
                "VnExpress",
                "Công nghệ",
                clock.instant());
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
