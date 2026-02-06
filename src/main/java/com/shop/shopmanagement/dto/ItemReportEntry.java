package com.shop.shopmanagement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ItemReportEntry {
    private String productName;
    private Double quantitySold;
    private BigDecimal totalRevenue;

    // Constructor 1: If Database returns Double (for 1.5 kg support)
    public ItemReportEntry(String productName, Double quantitySold, BigDecimal totalRevenue) {
        this.productName = productName;
        this.quantitySold = quantitySold != null ? quantitySold : 0.0;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    // Constructor 2: If Database returns Long (standard integer counts)
    // We convert it to Double automatically so the app doesn't crash
    public ItemReportEntry(String productName, Long quantitySold, BigDecimal totalRevenue) {
        this.productName = productName;
        this.quantitySold = quantitySold != null ? quantitySold.doubleValue() : 0.0;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
}