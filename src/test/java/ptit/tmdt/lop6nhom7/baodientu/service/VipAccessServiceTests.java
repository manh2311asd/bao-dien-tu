package ptit.tmdt.lop6nhom7.baodientu.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ptit.tmdt.lop6nhom7.baodientu.entity.User;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserRole;
import ptit.tmdt.lop6nhom7.baodientu.enums.UserStatus;
import ptit.tmdt.lop6nhom7.baodientu.exception.ForbiddenException;
import ptit.tmdt.lop6nhom7.baodientu.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
class VipAccessServiceTests {
  @Mock private UserRepo userRepo;
  @InjectMocks private VipAccessService vipAccessService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void paidMemberUsesVipFeaturesFromExpiryWithoutChangingBaseRole() {
    User member = user(31, UserRole.MEMBER, Instant.now().plusSeconds(3600));
    authenticate(member);

    assertSame(member, vipAccessService.requireCurrentVipOrAdmin());
  }

  @Test
  void expiredMemberCannotUseVipFeatures() {
    User member = user(32, UserRole.MEMBER, Instant.now().minusSeconds(1));
    authenticate(member);

    assertThrows(ForbiddenException.class, vipAccessService::requireCurrentVipOrAdmin);
  }

  @Test
  void adminCanInspectVipFeatureWithoutPersonalSubscription() {
    User admin = user(33, UserRole.ADMIN, null);
    authenticate(admin);

    assertSame(admin, vipAccessService.requireCurrentVipOrAdmin());
  }

  private void authenticate(User user) {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(user.getId(), null, List.of())
    );
    when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
  }

  private User user(Integer id, UserRole role, Instant expiry) {
    User user = new User();
    user.setId(id);
    user.setRole(role);
    user.setStatus(UserStatus.ACTIVE);
    user.setVipExpiryDate(expiry);
    return user;
  }
}
