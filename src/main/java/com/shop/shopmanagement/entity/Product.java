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

    private String category;



// --- STOCK LOGIC ---

    @Column(nullable = false)

    private Double totalStock;



    @Column(nullable = false)

    private String unitType;



// --- PRICING (BigDecimal) ---

    @Column(nullable = false, precision = 19, scale = 4)

    private BigDecimal costPricePerUnit;



    @Column(nullable = false, precision = 19, scale = 4)

    private BigDecimal retailPricePerUnit;



    @Column(precision = 19, scale = 4)

    private BigDecimal wholesalePricePerUnit;



// --- BATCH INFO ---

    private LocalDate buyingDate;

    private LocalDate expiryDate;

    private String supplierName;

    private String supplierPhone;



// --- NEW FIELD ADDED ---

    private String remarks; // For extra info like "Batch #123"



    public String getDisplayName() {

        return name + " (" + unitType + ")";

    }

}