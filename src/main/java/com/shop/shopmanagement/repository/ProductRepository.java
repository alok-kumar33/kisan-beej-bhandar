package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Helper to search by Name OR Unit Type
    List<Product> findByNameAndUnitType(String name, String unitType);

    // NEW: Find products where stock is dangerously low (less than X)
    List<Product> findByTotalStockLessThan(Double limit);
}