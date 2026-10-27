package ptit.tmdt.lop6nhom7.baodientu.rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Component
public class ExternalNewsFeedClient {
    private static final int MAX_ENTRIES_PER_FEED = 40;
    private static final String DEFAULT_IMAGE =
            "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800";

    public List<ExternalNewsDto> fetch(RssSourceProperties.FeedSource source) throws Exception {
        URLConnection connection = URI.create(source.getUrl()).toURL().openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", "NewsDaily/1.0");

        SyndFeed feed;
        try (InputStream stream = connection.getInputStream();
             XmlReader reader = new XmlReader(stream)) {
            feed = new SyndFeedInput().build(reader);
        }

        List<ExternalNewsDto> articles = new ArrayList<>();
        for (SyndEntry entry : feed.getEntries()) {
            if (articles.size() >= MAX_ENTRIES_PER_FEED) {
                break;
            }
            ExternalNewsDto article = normalize(source, entry);
            if (article != null) {
                articles.add(article);
            }
        }
        return articles;
    }

    private ExternalNewsDto normalize(RssSourceProperties.FeedSource source, SyndEntry entry) {
        String articleUrl = validHttpUrl(entry.getLink());
        String title = trim(entry.getTitle());
        if (articleUrl.isBlank() || title.isBlank()) {
            return null;
        }

        String description = "";
        String imageUrl = "";
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            Document document = Jsoup.parse(entry.getDescription().getValue());
            Element image = document.selectFirst("img[src]");
            if (image != null) {
                imageUrl = validHttpUrl(image.attr("src"));
            }
            description = document.text().trim();
        }
        if (imageUrl.isBlank()) {
            imageUrl = entry.getForeignMarkup().stream()
                    .map(element -> element.getAttributeValue("url"))
                    .map(this::validHttpUrl)
                    .filter(value -> !value.isBlank())
                    .findFirst()
                    .orElse(DEFAULT_IMAGE);
        }
        if (description.isBlank()) {
            description = title;
        }

        Instant publishedAt = entry.getPublishedDate() != null
                ? entry.getPublishedDate().toInstant()
                : entry.getUpdatedDate() != null
                        ? entry.getUpdatedDate().toInstant()
                        : Instant.EPOCH;
        String category = trim(source.getDefaultCategory());
        if (category.isBlank()) {
            category = "Tin mới nhất";
        }

        return new ExternalNewsDto(
                stableId(source.getName(), articleUrl),
                title,
                description,
                imageUrl,
                articleUrl,
                trim(source.getName()),
                category,
                publishedAt);
    }

    private String stableId(String source, String articleUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = normalizeKey(source) + "|" + articleUrl;
            String hash = HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
            return normalizeKey(source) + "_" + hash.substring(0, 16);
        } catch (Exception exception) {
            return normalizeKey(source) + "_" + Integer.toUnsignedString(articleUrl.hashCode(), 16);
        }
    }

    private String normalizeKey(String value) {
        return Normalizer.normalize(trim(value), Normalizer.Form.NFD)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private String validHttpUrl(String value) {
        String candidate = trim(value);
        try {
            URI uri = URI.create(candidate);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    ? candidate
                    : "";
        } catch (Exception exception) {
            return "";
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
