package ptit.tmdt.lop6nhom7.baodientu.controller;
 
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ptit.tmdt.lop6nhom7.baodientu.dto.VipPackageResponse;
import ptit.tmdt.lop6nhom7.baodientu.service.VipPackageService;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/vip-packages")
@RequiredArgsConstructor
public class VipPackageController {
 
    private final VipPackageService vipPackageService;
 
    /**
     * GET /api/vip-packages
     * Danh sách tất cả gói VIP — public, không cần đăng nhập.
     */
    @GetMapping
    public ResponseEntity<List<VipPackageResponse>> getAllPackages() {
        return ResponseEntity.ok(vipPackageService.getAllPackages());
    }
 
    /**
     * GET /api/vip-packages/{id}
     * Chi tiết một gói VIP — public, không cần đăng nhập.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VipPackageResponse> getPackageById(@PathVariable Integer id) {
        return ResponseEntity.ok(vipPackageService.getPackageById(id));
    }
}