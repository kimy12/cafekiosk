package sample.cafekiosk.spring.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    /**
     * select *
     * from product
     * where selling_status in ('SELLING', 'HOLD')
     * @param sellingStatuses
     * @return
     */
    List<Product> findAllBySellingStatusIn(List<ProductSellingStatus> sellingStatuses);
}
