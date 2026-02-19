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
    private String unitType;

    // 1. If Database returns Double & BigDecimal
    public ItemReportEntry(String productName, Double quantitySold, BigDecimal totalRevenue, String unitType) {
        this.productName = productName;
        this.quantitySold = quantitySold != null ? quantitySold : 0.0;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.unitType = unitType != null ? unitType : "";
    }

    // 2. If Database returns Long & BigDecimal
    public ItemReportEntry(String productName, Long quantitySold, BigDecimal totalRevenue, String unitType) {
        this.productName = productName;
        this.quantitySold = quantitySold != null ? quantitySold.doubleValue() : 0.0;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.unitType = unitType != null ? unitType : "";
    }

    // 3. NEW: If Database multiplies Double * BigDecimal, it returns a Double!
    public ItemReportEntry(String productName, Double quantitySold, Double totalRevenue, String unitType) {
        this.productName = productName;
        this.quantitySold = quantitySold != null ? quantitySold : 0.0;
        // Convert the incoming Double into a safe BigDecimal
        this.totalRevenue = totalRevenue != null ? BigDecimal.valueOf(totalRevenue) : BigDecimal.ZERO;
        this.unitType = unitType != null ? unitType : "";
    }

    // 4. NEW: Safest fallback for any Integer/Long * Double math combination
    public ItemReportEntry(String productName, Long quantitySold, Double totalRevenue, String unitType) {
        this.productName = productName;
        this.quantitySold = quantitySold != null ? quantitySold.doubleValue() : 0.0;
        this.totalRevenue = totalRevenue != null ? BigDecimal.valueOf(totalRevenue) : BigDecimal.ZERO;
        this.unitType = unitType != null ? unitType : "";
    }
}