package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.dto.SaleRequest;
import com.shop.shopmanagement.entity.Product;
import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.entity.SaleItem;
import com.shop.shopmanagement.service.ProductService;
import com.shop.shopmanagement.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal; // Security Import
import java.util.ArrayList;
import java.util.List;

@Controller
public class SaleController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    // 1. Show the "New Sale" Page
    @GetMapping("/sales/new")
    public String showSalePage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "sales"; // Renders sales.html
    }

    // 2. Handle the Checkout (AJAX Request)
    @PostMapping("/sales/save")
    @ResponseBody
    public Long createSale(@RequestBody SaleRequest request, Principal principal) {

        // Identify WHO is making this sale (Security)
        String currentUsername = principal != null ? principal.getName() : "Unknown";

        List<SaleItem> items = new ArrayList<>();

        for (SaleRequest.CartItem cartItem : request.getCartItems()) {
            Product p = new Product();
            p.setId(cartItem.getProductId());

            SaleItem item = new SaleItem();
            item.setProduct(p);
            item.setQuantitySold(cartItem.getQuantity());

            // IMPORTANT: Capture the specific price (Wholesale/Retail) sent from frontend
            item.setPricePerUnit(cartItem.getPrice());

            items.add(item);
        }

        Sale savedSale = saleService.createSale(
                items,
                request.getCustomerName(),
                request.getCustomerPhone(),
                request.getPaymentMode(),
                currentUsername // Pass the staff username
        );

        // Return the Sale ID so the frontend can redirect to the Invoice
        return savedSale.getId();
    }
}