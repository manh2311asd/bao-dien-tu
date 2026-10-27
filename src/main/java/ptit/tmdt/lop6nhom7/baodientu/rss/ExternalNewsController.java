package ptit.tmdt.lop6nhom7.baodientu.rss;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "External News", description = "Aggregated RSS news cached in memory and never persisted to Articles")
public class ExternalNewsController {
    private final NewsAggregatorService newsAggregatorService;

    @GetMapping
    @Operation(
            summary = "Get external aggregated news",
            description = "Reads a cached RSS snapshot. External news metadata is not persisted in MySQL or the Articles table.")
    public ExternalNewsPageResponse getNews(
            @Parameter(description = "Feed category, for example technology or Kinh doanh")
            @RequestParam(required = false) String category,
            @Parameter(description = "Publisher name, for example VnExpress")
            @RequestParam(required = false) String source,
            @Parameter(description = "Optional title or description search text")
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "Requests a refresh, limited to once per 30 seconds")
            @RequestParam(defaultValue = "false") boolean refresh) {
        return newsAggregatorService.getNews(category, source, keyword, page, pageSize, refresh);
    }
}
