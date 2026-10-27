package ptit.tmdt.lop6nhom7.baodientu.rss;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Backwards-compatible name for the former RSS import action.
 *
 * <p>The action now refreshes only the in-memory external-news cache. It does
 * not create Article entities and does not write RSS entries to MySQL.</p>
 */
@Service
@RequiredArgsConstructor
public class RssImportService {
    private final NewsAggregatorService newsAggregatorService;

    public void importAll() {
        newsAggregatorService.refreshCache();
    }
}
