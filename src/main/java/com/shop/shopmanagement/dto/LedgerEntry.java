package com.shop.shopmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LedgerEntry implements Comparable<LedgerEntry> {
    private LocalDateTime date;
    private String description; // e.g., "Sale #101" or "Cash Payment"
    private String type;        // "DEBIT" or "CREDIT"
    private BigDecimal amount;

    // Sort by Date (Newest first)
    @Override
    public int compareTo(LedgerEntry other) {
        return other.date.compareTo(this.date);
    }
}