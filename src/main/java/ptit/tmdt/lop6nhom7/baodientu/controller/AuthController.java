package ptit.tmdt.lop6nhom7.baodientu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ptit.tmdt.lop6nhom7.baodientu.dto.LoginReq;
import ptit.tmdt.lop6nhom7.baodientu.dto.LoginRes;
import ptit.tmdt.lop6nhom7.baodientu.dto.RegisterReq;
import ptit.tmdt.lop6nhom7.baodientu.service.AuthService;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  
  @PostMapping("/register")
  public ResponseEntity<Void> register(
      @RequestBody @Valid RegisterReq request
      ) {
    authService.register(request);
    return ResponseEntity.ok().build();
  }
  
  @PostMapping("/login")
  public ResponseEntity<LoginRes> login(
      @RequestBody @Valid LoginReq request) {
    return ResponseEntity.ok(authService.login(request));
  }
  
}
