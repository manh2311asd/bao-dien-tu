package ptit.tmdt.lop6nhom7.baodientu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ptit.tmdt.lop6nhom7.baodientu.dto.CreateArticleCommentRequest;
import ptit.tmdt.lop6nhom7.baodientu.entity.Article;
import ptit.tmdt.lop6nhom7.baodientu.entity.Comment;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;
import ptit.tmdt.lop6nhom7.baodientu.enums.ArticleStatus;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserRole;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserStatus;
import ptit.tmdt.lop6nhom7.baodientu.repository.ArticleRepo;
import ptit.tmdt.lop6nhom7.baodientu.repository.CommentRepo;
import ptit.tmdt.lop6nhom7.baodientu.service.ArticleCommentService;
import ptit.tmdt.lop6nhom7.baodientu.service.VipAccessService;

@ExtendWith(MockitoExtension.class)
class MemberCommentWorkflowTests {
  @Mock private CommentRepo commentRepo;
  @Mock private ArticleRepo articleRepo;
  @Mock private VipAccessService vipAccessService;
  @InjectMocks private ArticleCommentService articleCommentService;

  @Test
  void activeMemberCanCommentOnPublishedArticleWithoutVip() {
    Article article = new Article();
    article.setId(31);
    article.setTitle("Bai viet da xuat ban");
    article.setStatus(ArticleStatus.PUBLISHED);
    User member = new User();
    member.setId(12);
    member.setFullName("Doc gia");
    member.setRole(UserRole.MEMBER);
    member.setStatus(UserStatus.ACTIVE);
    CreateArticleCommentRequest request = new CreateArticleCommentRequest();
    request.setContent("  Binh luan hop le  ");

    when(articleRepo.findByIdAndStatus(31, ArticleStatus.PUBLISHED))
        .thenReturn(Optional.of(article));
    when(vipAccessService.requireCurrentUser()).thenReturn(member);
    when(commentRepo.save(any(Comment.class))).thenAnswer(invocation -> {
      Comment saved = invocation.getArgument(0);
      saved.setId(91);
      return saved;
    });

    var response = articleCommentService.createComment(31, request);

    assertEquals(91, response.getId());
    assertEquals(12, response.getUserId());
    assertEquals("Binh luan hop le", response.getContent());
    verify(vipAccessService).requireCurrentUser();
  }
}
