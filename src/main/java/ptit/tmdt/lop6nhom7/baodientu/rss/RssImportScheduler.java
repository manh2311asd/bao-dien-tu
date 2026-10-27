package ptit.tmdt.lop6nhom7.baodientu.rss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "news.rss.scheduled-refresh-enabled", havingValue = "true")
public class RssImportScheduler {

    private final RssImportService rssImportService;

    // Optional cache pre-warming. Disabled by default; it never writes to MySQL.
    @Scheduled(fixedDelayString = "${news.rss.refresh-delay-ms:900000}")
    public void importNews() {
        log.info("Starting scheduled RSS news import job...");
        try {
            rssImportService.importAll();
        } catch (Exception e) {
            log.error("Error occurred during scheduled RSS import job", e);
        }
    }
}
