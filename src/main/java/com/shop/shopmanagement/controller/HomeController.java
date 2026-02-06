package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.repository.ProductRepository;
import com.shop.shopmanagement.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Quick Stats for the Dashboard
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("saleCount", saleRepository.count()); // Total lifetime sales

        // In a real app, we would query "Sales Today" specifically,
        // but for now, total count is a good placeholder.
        model.addAttribute("date", LocalDate.now());

        return "index"; // Looks for index.html
    }
}