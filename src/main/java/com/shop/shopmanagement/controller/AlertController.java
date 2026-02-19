package com.shop.shopmanagement.controller;



import com.shop.shopmanagement.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;



import java.time.LocalDate;



@Controller

public class AlertController {



    @Autowired

    private ProductRepository productRepository;



    @GetMapping("/alerts")

    public String showAlertsPage(Model model) {

// 1. Low Stock (Items with stock < 10)

        model.addAttribute("lowStockItems", productRepository.findByTotalStockLessThan(10.0));



// 2. Expiring Soon (Items expiring in next 30 days)

        LocalDate today = LocalDate.now();

        LocalDate nextMonth = today.plusDays(30);

        model.addAttribute("expiringItems", productRepository.findByExpiryDateBetween(today, nextMonth));



        return "alerts"; // Renders alerts.html

    }

}

