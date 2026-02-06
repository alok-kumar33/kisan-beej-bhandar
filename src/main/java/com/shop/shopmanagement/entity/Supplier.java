package com.shop.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends BaseEntity {

    @Column(nullable = false)
    private String name; // Contact Person Name

    private String agencyName; // Shop/Company Name (e.g. "Sharma Distributors")

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String gstNumber;
    private String address;

    // ACCOUNTING
    // Positive Value = We Owe THEM money (Payable)
    @Column(nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;
}