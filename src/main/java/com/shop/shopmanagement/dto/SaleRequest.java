package com.shop.shopmanagement.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleRequest {
    private String customerName;
    private String customerPhone;
    private String paymentMode;

    // A list of small objects representing rows in the cart
    private List<CartItem> cartItems;

    @Getter
    @Setter
    public static class CartItem {
        private Long productId;
        private Double quantity;

        // ADDED THIS FIELD: To receive the specific price (Wholesale/Retail) used
        private BigDecimal price;
    }
}