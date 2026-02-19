package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    // Find all items belonging to a specific sale bill
    List<SaleItem> findBySaleId(Long saleId);

    // 👇 DELETE OR COMMENT OUT THE OLD @Query FOR getItemPerformance 👇
    // It is no longer needed because ReportService handles it directly!
}