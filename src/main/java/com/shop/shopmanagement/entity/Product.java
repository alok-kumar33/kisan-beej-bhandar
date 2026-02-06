package com.shop.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // Added "Equipment" support in UI

    // --- STOCK LOGIC (Stored in Smallest Base Unit) ---

    @Column(nullable = false)
    private Double totalStock; // Stores 10000.0 (grams)

    @Column(nullable = false)
    private String unitType; // "g", "ml", "pcs" (The smallest unit you sell)

    // --- PRICING (Stored Per Base Unit) ---
    // Example: If 1g costs 0.5 paisa, we store 0.005
    // We will calculate this automatically from your inputs

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal costPricePerUnit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal retailPricePerUnit;

    @Column(precision = 19, scale = 4)
    private BigDecimal wholesalePricePerUnit; // Made OPTIONAL (No 'nullable=false')

    // --- BATCH INFO ---
    private LocalDate buyingDate;
    private LocalDate expiryDate;
    private String supplierName;
    private String supplierPhone;

    // Just for display (e.g., "Rat Poison")
    public String getDisplayName() {
        return name + " (" + unitType + ")";
    }
}