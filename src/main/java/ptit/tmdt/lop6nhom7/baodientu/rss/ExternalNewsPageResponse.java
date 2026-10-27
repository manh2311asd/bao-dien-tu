package ptit.tmdt.lop6nhom7.baodientu.rss;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Paginated snapshot of external aggregated news")
public record ExternalNewsPageResponse(
        Instant updatedAt,
        int page,
        int pageSize,
        long total,
        boolean hasMore,
        List<ExternalNewsDto> items) {
}
