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

        // A. Add Sales (DEBIT - Money Owed to Us)
        // We need a method in SaleRepo to find by Customer ID.
        // *Assuming we fetch all sales and filter, or add method to Repo*
        // Let's stick to Repo method for performance.
        List<Sale> sales = saleRepository.findByCustomerPhone(
                customerRepository.findById(customerId).get().getPhoneNumber()
        );

        for (Sale s : sales) {
            // Only count Credit sales as "Debt"?
            // Actually, the ledger should show ALL transactions.
            // But usually, only 'Credit' payment mode increases debt.
            // However, to show full history, let's show the Sale as DEBIT
            // If they paid Cash, we can show a matching CREDIT immediately,
            // but for simplicity, let's focus on the DEBT aspect.
            // Better Approach: Show Transaction.
            // If PaymentMode = Credit -> DEBIT Amount.

            if ("Credit".equalsIgnoreCase(s.getPaymentMode())) {
                ledger.add(new LedgerEntry(
                        s.getSaleDate(),
                        "Goods Purchase (Bill #" + s.getId() + ")",
                        "DEBIT",
                        s.getTotalAmount()
                ));
            }
        }

        // B. Add Payments (CREDIT - Money Paid by Customer)
        List<Payment> payments = paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId);
        for (Payment p : payments) {
            ledger.add(new LedgerEntry(
                    p.getPaymentDate(),
                    "Payment Received (" + p.getPaymentMode() + ")",
                    "CREDIT",
                    p.getAmount()
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

        customer.setCurrentBalance(customer.getCurrentBalance().subtract(amount));
        customerRepository.save(customer);

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMode(mode);
        payment.setRemarks(remarks);
        paymentRepository.save(payment);
    }

    // 3. UPDATE PROFILE
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