package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.repository.ProductRepository;
import com.shop.shopmanagement.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @GetMapping("/")
    public String home(Model model) {
        // NOTE: We removed the Low Stock & Expiry logic from here.
        // It is now handled by AlertController.java.
        // This keeps the dashboard fast and clean!

        // 1. Stats for the Boxes (Total Items & Total Sales)
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("saleCount", saleRepository.count());

        // 2. Date
        model.addAttribute("date", LocalDate.now());

        return "index";
    }
}