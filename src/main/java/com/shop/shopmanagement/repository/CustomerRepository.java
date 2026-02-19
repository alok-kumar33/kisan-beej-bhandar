package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // NEW: Fetch all customers who owe money (balance > 0), sorted by highest debt first
    List<Customer> findByCurrentBalanceGreaterThanOrderByCurrentBalanceDesc(BigDecimal amount);
}