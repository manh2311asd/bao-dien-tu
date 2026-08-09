package ptit.tmdt.lop6nhom7.baodientu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ptit.tmdt.lop6nhom7.baodientu.dto.AdminVipPackageUpdateReq;
import ptit.tmdt.lop6nhom7.baodientu.dto.VipPackageRes;
import ptit.tmdt.lop6nhom7.baodientu.service.AdminVipPackageService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vip-packages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVipPackageController {
  private final AdminVipPackageService adminVipPackageService;

  @GetMapping
  public List<VipPackageRes> getAllPackages() {
    return adminVipPackageService.getAllPackages();
  }

  @GetMapping("/{id}")
  public VipPackageRes getPackageById(@PathVariable Integer id) {
    return adminVipPackageService.getPackageById(id);
  }

  @PutMapping("/{id}")
  public VipPackageRes updatePackage(
      @PathVariable Integer id,
      @RequestBody @Valid AdminVipPackageUpdateReq request
  ) {
    return adminVipPackageService.updatePackage(id, request);
  }
}
