package ptit.tmdt.lop6nhom7.baodientu.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HtmlSanitizerServiceTests {
  private final HtmlSanitizerService sanitizer = new HtmlSanitizerService();

  @Test
  void removesExecutableHtmlAndUnsafeProtocols() {
    String sanitized = sanitizer.sanitizeArticleHtml("""
        <p onclick="alert(1)">Nội dung</p>
        <script>alert('xss')</script>
        <img src="javascript:alert(1)" onerror="alert(2)">
        <a href="javascript:alert(3)">Mở liên kết</a>
        """);

    assertTrue(sanitized.contains("<p>Nội dung</p>"));
    assertFalse(sanitized.contains("script"));
    assertFalse(sanitized.contains("onclick"));
    assertFalse(sanitized.contains("onerror"));
    assertFalse(sanitized.contains("javascript:"));
    assertTrue(sanitizer.hasReadableText(sanitized));
  }

  @Test
  void keepsSafeArticleFormattingAndMedia() {
    String sanitized = sanitizer.sanitizeArticleHtml("""
        <h2>Tiêu đề phụ</h2>
        <p>Nội dung <strong>quan trọng</strong>.</p>
        <figure><img src="https://example.com/news.jpg" alt="Ảnh"><figcaption>Chú thích</figcaption></figure>
        """);

    assertTrue(sanitized.contains("<h2>Tiêu đề phụ</h2>"));
    assertTrue(sanitized.contains("<strong>quan trọng</strong>"));
    assertTrue(sanitized.contains("https://example.com/news.jpg"));
    assertTrue(sanitized.contains("<figcaption>Chú thích</figcaption>"));
    assertTrue(sanitizer.hasReadableText(sanitized));
  }
}
