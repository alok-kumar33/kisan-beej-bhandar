package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.dto.ItemReportEntry;
import com.shop.shopmanagement.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT new com.shop.shopmanagement.dto.ItemReportEntry(s.product.name, SUM(s.quantitySold), SUM(s.subTotal)) " +
            "FROM SaleItem s WHERE s.sale.saleDate BETWEEN :start AND :end " +
            "GROUP BY s.product.name ORDER BY SUM(s.quantitySold) DESC")
    List<ItemReportEntry> getItemPerformance(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}