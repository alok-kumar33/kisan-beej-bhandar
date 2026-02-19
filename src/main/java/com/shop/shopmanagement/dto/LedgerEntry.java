package com.shop.shopmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // Added for flexibility
public class LedgerEntry implements Comparable<LedgerEntry> {

    private LocalDateTime date;
    private String description;
    private String type;        // "DEBIT" or "CREDIT"
    private BigDecimal amount;
    private String remarks;     // The new field

    // --- COMPATIBILITY CONSTRUCTOR (Fixes PurchaseService Error) ---
    // This allows the old code to work by automatically setting remarks to ""
    public LedgerEntry(LocalDateTime date, String description, String type, BigDecimal amount) {
        this.date = date;
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.remarks = ""; // Default empty remark
    }

    // Sort by Date (Newest first)
    @Override
    public int compareTo(LedgerEntry other) {
        return other.date.compareTo(this.date);
    }
}