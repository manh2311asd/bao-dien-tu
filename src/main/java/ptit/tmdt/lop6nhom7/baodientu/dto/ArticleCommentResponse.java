package ptit.tmdt.lop6nhom7.baodientu.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCommentResponse {
  private Integer id;
  private Integer articleId;
  private Integer userId;
  private String userName;
  private String content;
  private Instant createdAt;

  private CommentUserDto user;
  private CommentArticleDto article;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentUserDto {
    private Integer id;
    private String displayName;
    private String avatarUrl;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentArticleDto {
    private Integer id;
    private String title;
  }
}