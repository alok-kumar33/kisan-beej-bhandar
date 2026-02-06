package com.shop.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String address;

    // --- NEW FIELDS FOR RETAILERS ---
    private String customerType; // "RETAIL" or "WHOLESALE"
    private String shopName;
    private String panNumber;
    private String idProofType; // e.g. Aadhar, Voter ID
    private String idProofNumber;

    // LEDGER
    // Positive = They OWE us. Negative = We owe them (Advance).
    @Column(nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;
}