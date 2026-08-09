package ptit.tmdt.lop6nhom7.baodientu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.tmdt.lop6nhom7.baodientu.entity.VipPackage;

@Repository
public interface VipPackageRepo extends JpaRepository<VipPackage, Integer> {
}
