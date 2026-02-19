package com.shop.shopmanagement.repository;



import com.shop.shopmanagement.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;



import java.time.LocalDate;

import java.util.List;



@Repository

public interface ProductRepository extends JpaRepository<Product, Long> {



// 1. Search Bar

    List<Product> findByNameContainingIgnoreCase(String name);



// 2. Low Stock Alerts

    List<Product> findByTotalStockLessThan(Double stockLimit);



// 3. Duplicate Check

    List<Product> findByNameAndUnitType(String name, String unitType);



// 4. NEW: Find items expiring between today and next 30 days

    List<Product> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

}