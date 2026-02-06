package com.shop.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem extends BaseEntity {

    // WHICH SALE DOES THIS BELONG TO?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    // WHICH PRODUCT WAS SOLD?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // HOW MUCH?
    @Column(nullable = false)
    private Double quantitySold; // e.g. 5.0

    @Column(nullable = false)
    private String unitType; // e.g. "kg" (Just for record keeping)

    // PRICE AT THE TIME OF SALE
    // (We store this because product price might change later, but old bills shouldn't change)
    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private BigDecimal subTotal; // quantity * pricePerUnit
}