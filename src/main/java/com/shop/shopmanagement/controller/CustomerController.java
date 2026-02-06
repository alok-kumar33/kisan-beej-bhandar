package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // This URL will be called by JavaScript when you type the phone number
    @GetMapping("/customers/search")
    public ResponseEntity<Customer> getCustomerByPhone(@RequestParam String phone) {
        return customerRepository.findByPhoneNumber(phone)
                .map(ResponseEntity::ok) // Return 200 OK + Customer Data
                .orElse(ResponseEntity.notFound().build()); // Return 404 Not Found
    }
}