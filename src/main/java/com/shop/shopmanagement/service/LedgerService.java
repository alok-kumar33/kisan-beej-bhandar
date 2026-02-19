package com.shop.shopmanagement.service;

import com.shop.shopmanagement.dto.LedgerEntry;
import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.entity.Payment;
import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.repository.CustomerRepository;
import com.shop.shopmanagement.repository.PaymentRepository;
import com.shop.shopmanagement.repository.SaleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LedgerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SaleRepository saleRepository;

    // 1. GET UNIFIED HISTORY
    public List<LedgerEntry> getCustomerLedger(Long customerId) {
        List<LedgerEntry> ledger = new ArrayList<>();

        // Fetch Customer to get Phone (Your logic)
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // A. Add Sales (DEBIT) using your specific repo method
        List<Sale> sales = saleRepository.findByCustomerPhone(customer.getPhoneNumber());

        for (Sale s : sales) {
            // Only adding "Credit" sales to ledger as per your logic
            if ("Credit".equalsIgnoreCase(s.getPaymentMode())) {
                ledger.add(new LedgerEntry(
                        s.getSaleDate(),
                        "Goods Purchase (Bill #" + s.getId() + ")",
                        "DEBIT",
                        s.getTotalAmount(),
                        "" // Sales usually don't have remarks, sending empty string
                ));
            }
        }

        // B. Add Payments (CREDIT)
        List<Payment> payments = paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId);
        for (Payment p : payments) {
            ledger.add(new LedgerEntry(
                    p.getPaymentDate(),
                    "Payment Received (" + p.getPaymentMode() + ")",
                    "CREDIT",
                    p.getAmount(),
                    p.getRemarks() // <--- PASSING THE REMARKS HERE
            ));
        }

        // Sort by Date
        Collections.sort(ledger);
        return ledger;
    }

    // 2. COLLECT PAYMENT
    @Transactional
    public void collectPayment(Long customerId, BigDecimal amount, String mode, String remarks) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Update Balance (Your logic)
        customer.setCurrentBalance(customer.getCurrentBalance().subtract(amount));
        customerRepository.save(customer);

        // Save Payment
        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMode(mode);
        payment.setRemarks(remarks); // Saving remarks to DB
        paymentRepository.save(payment);
    }

    // 3. UPDATE PROFILE (Keeping your exact fields)
    public void updateCustomerProfile(Long customerId, Customer updatedInfo) {
        Customer dbCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        dbCustomer.setName(updatedInfo.getName());
        dbCustomer.setAddress(updatedInfo.getAddress());
        dbCustomer.setCustomerType(updatedInfo.getCustomerType());
        dbCustomer.setShopName(updatedInfo.getShopName());
        dbCustomer.setPanNumber(updatedInfo.getPanNumber());
        dbCustomer.setIdProofType(updatedInfo.getIdProofType());
        dbCustomer.setIdProofNumber(updatedInfo.getIdProofNumber());

        customerRepository.save(dbCustomer);
    }
}