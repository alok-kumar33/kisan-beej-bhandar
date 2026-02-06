package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);

    // NEW: For Day Book
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
}