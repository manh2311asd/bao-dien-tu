package ptit.tmdt.lop6nhom7.baodientu.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {
  private static final Safelist ARTICLE_SAFELIST = Safelist.relaxed()
      .addTags("figure", "figcaption", "h1", "h2", "h3", "h4", "h5", "h6")
      .addAttributes("img", "width", "height", "loading")
      .addAttributes("a", "target", "rel")
      .addProtocols("a", "href", "http", "https", "mailto")
      .addProtocols("img", "src", "http", "https");

  private static final Document.OutputSettings OUTPUT_SETTINGS =
      new Document.OutputSettings().prettyPrint(false);

  public String sanitizeArticleHtml(String html) {
    if (html == null || html.isBlank()) {
      return "";
    }
    return Jsoup.clean(html.trim(), "", ARTICLE_SAFELIST, OUTPUT_SETTINGS);
  }

  public boolean hasReadableText(String sanitizedHtml) {
    return sanitizedHtml != null && !Jsoup.parse(sanitizedHtml).text().isBlank();
  }
}
