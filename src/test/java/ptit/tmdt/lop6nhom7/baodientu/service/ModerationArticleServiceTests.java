package ptit.tmdt.lop6nhom7.baodientu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ptit.tmdt.lop6nhom7.baodientu.entity.Article;
import ptit.tmdt.lop6nhom7.baodientu.entity.Category;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;
import ptit.tmdt.lop6nhom7.baodientu.enums.ArticleOrigin;
import ptit.tmdt.lop6nhom7.baodientu.enums.ArticleStatus;
import ptit.tmdt.lop6nhom7.baodientu.enums.ArticleType;
import ptit.tmdt.lop6nhom7.baodientu.repository.ArticleRepo;

@ExtendWith(MockitoExtension.class)
class ModerationArticleServiceTests {
  private static final List<ArticleStatus> VISIBILITY_STATUSES = List.of(
      ArticleStatus.PUBLISHED,
      ArticleStatus.HIDDEN
  );

  @Mock private ArticleRepo articleRepo;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private NewsNotificationService newsNotificationService;
  @Mock private HtmlSanitizerService htmlSanitizerService;
  @InjectMocks private ModerationArticleService moderationArticleService;

  @Test
  void visibilityListUsesInternalArticlesOnly() {
    Article internalArticle = article(21, ArticleOrigin.INTERNAL);
    when(articleRepo.findInternalByStatusInOrderByCreatedAtDesc(VISIBILITY_STATUSES))
        .thenReturn(List.of(internalArticle));

    var result = moderationArticleService.getVisibilityManagedArticles(null);

    assertEquals(1, result.size());
    assertEquals(ArticleOrigin.INTERNAL, result.get(0).getOrigin());
    verify(articleRepo).findInternalByStatusInOrderByCreatedAtDesc(VISIBILITY_STATUSES);
    verify(articleRepo, never()).findByStatusInOrderByCreatedAtDesc(VISIBILITY_STATUSES);
  }

  @Test
  void externalRssArticleCannotAppearInNumericVisibilitySearch() {
    Article externalArticle = article(22, ArticleOrigin.EXTERNAL);
    when(articleRepo.findById(22)).thenReturn(Optional.of(externalArticle));

    var result = moderationArticleService.getVisibilityManagedArticles("22");

    assertTrue(result.isEmpty());
  }

  private Article article(Integer id, ArticleOrigin origin) {
    User author = new User();
    author.setId(10);
    author.setFullName("Tác giả");
    Category category = new Category();
    category.setId(3);
    category.setName("Công nghệ");

    Article article = new Article();
    article.setId(id);
    article.setAuthor(author);
    article.setCategory(category);
    article.setCoverImage("https://example.com/cover.jpg");
    article.setTitle("Bài viết nội bộ");
    article.setSapo("Sapo");
    article.setContent("Nội dung");
    article.setType(ArticleType.FREE);
    article.setStatus(ArticleStatus.PUBLISHED);
    article.setViewCount(0);
    article.setOrigin(origin);
    return article;
  }
}
