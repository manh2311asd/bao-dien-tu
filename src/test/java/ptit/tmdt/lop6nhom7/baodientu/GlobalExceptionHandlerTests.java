package ptit.tmdt.lop6nhom7.baodientu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import ptit.tmdt.lop6nhom7.baodientu.dto.ErrorResponse;
import ptit.tmdt.lop6nhom7.baodientu.exception.GlobalExceptionHandler;

class GlobalExceptionHandlerTests {

  @Test
  void methodSecurityAccessDeniedReturnsForbiddenInsteadOfServerError() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    var response = handler.handleAccessDenied(new AccessDeniedException("denied"));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals(403, body.getStatus());
  }
}
