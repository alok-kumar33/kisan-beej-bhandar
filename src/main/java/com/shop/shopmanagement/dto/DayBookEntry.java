package com.shop.shopmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DayBookEntry implements Comparable<DayBookEntry> {
    private LocalDateTime time;
    private String type;        // "SALE", "EXPENSE", "PAYMENT_IN", "PAYMENT_OUT"
    private String description; // "Sold Urea", "Tea Bill", "Recv from Ramesh"
    private BigDecimal amountIn; // Money Coming In
    private BigDecimal amountOut; // Money Going Out
    private String mode;        // Cash/Online

    @Override
    public int compareTo(DayBookEntry other) {
        return this.time.compareTo(other.time); // Sort by Time (Morning to Evening)
    }
}