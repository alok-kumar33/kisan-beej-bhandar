package com.shop.shopmanagement.repository;

import com.shop.shopmanagement.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Find expenses between two dates (for Reports)
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDateTime start, LocalDateTime end);
}