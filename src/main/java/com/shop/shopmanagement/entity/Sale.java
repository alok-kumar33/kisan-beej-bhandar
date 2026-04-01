package com.shop.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale extends BaseEntity {

    // DATE & TIME
    @Column(nullable = false)
    private LocalDateTime saleDate;

    // CUSTOMER INFO (Optional for quick sales, Required for Credit/Retailers)
    private String customerName;
    private String customerPhone;

    private String createdBy; // Stores "Ramesh (Staff)" or "Owner"

    // DISCOUNT AMOUNT (New Field!)
    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    // TOTAL BILL AMOUNT (This will now be: Sum of Items - Discount)
    @Column(nullable = false)
    private BigDecimal totalAmount;

    // PAYMENT MODE (Cash, UPI, Credit)
    private String paymentMode;

    // RELATIONSHIP: One Sale has Many Items
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    // Helper to add items easily
    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }
}