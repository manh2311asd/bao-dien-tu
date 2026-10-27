package ptit.tmdt.lop6nhom7.baodientu.rss;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "External news metadata read from RSS. This object is never persisted to Articles.")
public record ExternalNewsDto(
        @Schema(example = "vnexpress_7e61d5887b2c6768") String id,
        String title,
        String description,
        String imageUrl,
        String articleUrl,
        String source,
        String category,
        Instant publishedAt) {
}
