package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
    List<SupplierPayment> findBySupplierId(Long supplierId);

    // NEW: For Day Book
    List<SupplierPayment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
}