package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.dto.LedgerEntry; // FIXED: Now pointing to the 'dto' folder!
import com.shop.shopmanagement.repository.CustomerRepository;
import com.shop.shopmanagement.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
public class LedgerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LedgerService ledgerService;

    // 1. Show Search Page
    @GetMapping("/ledger")
    public String showLedgerSearch(Model model) {
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
        String phone = customerRepository.findById(customerId).get().getPhoneNumber();
        return "redirect:/ledger/view?phone=" + phone;
    }

    // 4. Update Profile Info
    @PostMapping("/ledger/update-profile")
    public String updateProfile(Customer customer) {
        ledgerService.updateCustomerProfile(customer.getId(), customer);
        return "redirect:/ledger/view?phone=" + customer.getPhoneNumber();
    }

    // ==========================================
    // WHATSAPP STATEMENT GENERATOR
    // ==========================================
    @GetMapping("/whatsapp/send-statement")
    public String sendWhatsappStatement(@RequestParam("phone") String phone) {

        Customer customer = customerRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Fetch their recent transactions using the exact DTO class
        List<LedgerEntry> ledgerEntries = ledgerService.getCustomerLedger(customer.getId());

        StringBuilder msg = new StringBuilder();
        msg.append("Namaste ").append(customer.getName()).append(",\n\n");
        msg.append("\nThis is an automated reminder from Kisan Beej Bhandar.\n");
        msg.append("\nTotal Pending Udhaar: Rs. ").append(customer.getCurrentBalance()).append("\n\n");
        msg.append("\nPlease settle this balance at your earliest convenience. \n Thank you!");

        // URL Encode the message
        String encodedMessage = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8);

        // Redirect directly to WhatsApp
        return "redirect:https://wa.me/977" + phone + "?text=" + encodedMessage;
    }
}