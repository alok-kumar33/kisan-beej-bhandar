package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.repository.CustomerRepository;
import com.shop.shopmanagement.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class LedgerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LedgerService ledgerService;

    // 1. Show Search Page (UPDATED: Sends Customer List for Auto-Complete)
    @GetMapping("/ledger")
    public String showLedgerSearch(Model model) {
        // This list powers the "Type Name" feature
        model.addAttribute("customers", customerRepository.findAll());
        return "ledger-search";
    }

    // 2. Show Customer Details & Unified History
    @GetMapping("/ledger/view")
    public String viewLedger(@RequestParam("phone") String phone, Model model) {
        Optional<Customer> customerOpt = customerRepository.findByPhoneNumber(phone);

        if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();
            model.addAttribute("customer", c);
            model.addAttribute("ledgerEntries", ledgerService.getCustomerLedger(c.getId()));
            return "ledger-view";
        } else {
            // If not found, reload search page with error AND the list again
            model.addAttribute("error", "Customer not found!");
            model.addAttribute("customers", customerRepository.findAll());
            return "ledger-search";
        }
    }

    // 3. Handle Payment Collection
    @PostMapping("/ledger/pay")
    public String makePayment(@RequestParam Long customerId,
                              @RequestParam BigDecimal amount,
                              @RequestParam String mode,
                              @RequestParam String remarks,
                              @RequestParam(required = false) String phoneRedirect) {

        ledgerService.collectPayment(customerId, amount, mode, remarks);

        // Fetch phone to redirect properly
        String phone = customerRepository.findById(customerId).get().getPhoneNumber();
        return "redirect:/ledger/view?phone=" + phone;
    }

    // 4. Update Profile Info
    @PostMapping("/ledger/update-profile")
    public String updateProfile(Customer customer) {
        ledgerService.updateCustomerProfile(customer.getId(), customer);
        return "redirect:/ledger/view?phone=" + customer.getPhoneNumber();
    }
}