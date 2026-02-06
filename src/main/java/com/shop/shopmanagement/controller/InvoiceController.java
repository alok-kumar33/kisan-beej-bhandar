package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.repository.CustomerRepository; // Import this
import com.shop.shopmanagement.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InvoiceController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private CustomerRepository customerRepository; // Inject Customer Repo

    @GetMapping("/sale/invoice/{id}")
    public String showInvoice(@PathVariable Long id, Model model) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        model.addAttribute("sale", sale);

        // LOOK UP CUSTOMER DETAILS
        // If the sale has a phone number, check if we have their Shop/PAN info
        if (sale.getCustomerPhone() != null && !sale.getCustomerPhone().isEmpty()) {
            customerRepository.findByPhoneNumber(sale.getCustomerPhone())
                    .ifPresent(customer -> model.addAttribute("customer", customer));
        }

        return "invoice";
    }
}