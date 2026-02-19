package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // --- 1. SEARCH CUSTOMER (Used by Sales Page Auto-Pop up) ---
    @GetMapping("/customers/search")
    @ResponseBody // <--- Crucial: Returns JSON data, not a HTML page
    public ResponseEntity<Customer> searchCustomer(@RequestParam String phone) {
        Optional<Customer> customer = customerRepository.findByPhoneNumber(phone);

        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- 2. ADD NEW CUSTOMER (Used by Ledger Search Page) ---
    @PostMapping("/customers/add")
    public String addCustomer(@RequestParam String name,
                              @RequestParam String phoneNumber,
                              @RequestParam(required = false, defaultValue = "0.0") Double initialBalance) {

        // Check duplication
        if (customerRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            return "redirect:/ledger?error=Customer already exists!";
        }

        // Create
        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setPhoneNumber(phoneNumber);
        newCustomer.setCustomerType("RETAIL");

        // Set Old Balance
        newCustomer.setCurrentBalance(BigDecimal.valueOf(initialBalance));

        customerRepository.save(newCustomer);

        // Go to Ledger
        return "redirect:/ledger/view?phone=" + phoneNumber;
    }

    // --- 3. MARKET COLLECTABLES REPORT (NEW) ---
    @GetMapping("/customers/report")
    public String viewCollectablesReport(Model model) {
        // 1. Get all customers who owe money (balance > 0)
        List<Customer> debtors = customerRepository.findByCurrentBalanceGreaterThanOrderByCurrentBalanceDesc(BigDecimal.ZERO);

        // 2. Calculate the Grand Total Collectable
        BigDecimal totalCollectable = debtors.stream()
                .map(Customer::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Send data to the HTML page
        model.addAttribute("debtors", debtors);
        model.addAttribute("totalCollectable", totalCollectable);

        return "customer-report"; // Looks for customer-report.html
    }
}