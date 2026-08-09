package ptit.tmdt.lop6nhom7.baodientu.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserRole;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserStatus;
import ptit.tmdt.lop6nhom7.baodientu.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTests {
  @Mock private JwtService jwtService;
  @Mock private UserRepo userRepo;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;
  @InjectMocks private JwtAuthFilter filter;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void activeTokenUsesCurrentDatabaseRole() throws Exception {
    User user = user(21, UserRole.AUTHOR, UserStatus.ACTIVE);
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwtService.isTokenValid("token")).thenReturn(true);
    when(jwtService.extractUserId("token")).thenReturn(21);
    when(userRepo.findById(21)).thenReturn(Optional.of(user));

    filter.doFilterInternal(request, response, filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertEquals(21, authentication.getPrincipal());
    assertEquals("ROLE_AUTHOR", authentication.getAuthorities().iterator().next().getAuthority());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void lockedAccountCannotKeepUsingAnOldToken() throws Exception {
    User user = user(22, UserRole.ADMIN, UserStatus.LOCKED);
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwtService.isTokenValid("token")).thenReturn(true);
    when(jwtService.extractUserId("token")).thenReturn(22);
    when(userRepo.findById(22)).thenReturn(Optional.of(user));

    filter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  private User user(Integer id, UserRole role, UserStatus status) {
    User user = new User();
    user.setId(id);
    user.setRole(role);
    user.setStatus(status);
    return user;
  }
}
