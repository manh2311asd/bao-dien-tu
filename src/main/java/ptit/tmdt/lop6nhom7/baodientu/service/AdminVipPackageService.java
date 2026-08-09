package ptit.tmdt.lop6nhom7.baodientu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptit.tmdt.lop6nhom7.baodientu.dto.AdminVipPackageUpdateReq;
import ptit.tmdt.lop6nhom7.baodientu.dto.VipPackageRes;
import ptit.tmdt.lop6nhom7.baodientu.entity.VipPackage;
import ptit.tmdt.lop6nhom7.baodientu.exception.NotFoundException;
import ptit.tmdt.lop6nhom7.baodientu.repository.VipPackageRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVipPackageService {
  private final VipPackageRepo vipPackageRepo;

  @Transactional(readOnly = true)
  public List<VipPackageRes> getAllPackages() {
    return vipPackageRepo.findAll()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public VipPackageRes getPackageById(Integer packageId) {
    return toResponse(findPackage(packageId));
  }

  @org.springframework.cache.annotation.CacheEvict(value = "vipPackages", allEntries = true)
  @Transactional
  public VipPackageRes updatePackage(Integer packageId, AdminVipPackageUpdateReq request) {
    VipPackage vipPackage = findPackage(packageId);
    vipPackage.setName(request.getName().trim());
    vipPackage.setDurationDays(request.getDurationDays());
    vipPackage.setPrice(request.getPrice());
    vipPackage.setDiscountPercent(request.getDiscountPercent());
    vipPackage.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
    return toResponse(vipPackageRepo.save(vipPackage));
  }

  private VipPackage findPackage(Integer packageId) {
    return vipPackageRepo.findById(packageId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay goi VIP voi id = " + packageId));
  }

  private VipPackageRes toResponse(VipPackage vipPackage) {
    return new VipPackageRes(
        vipPackage.getId(),
        vipPackage.getName(),
        vipPackage.getDurationDays(),
        vipPackage.getPrice(),
        vipPackage.getDiscountPercent(),
        vipPackage.getDescription()
    );
  }
}
