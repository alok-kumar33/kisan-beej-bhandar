package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Find all bills for a specific supplier, newest first
    List<Purchase> findBySupplierIdOrderByPurchaseDateDesc(Long supplierId);
}