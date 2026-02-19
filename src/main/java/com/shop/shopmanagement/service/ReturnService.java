package com.shop.shopmanagement.service;

import com.shop.shopmanagement.entity.*;
import com.shop.shopmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReturnService {

    @Autowired private SaleRepository saleRepository;
    @Autowired private SaleItemRepository saleItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private PaymentRepository paymentRepository;

    @Transactional
    public void processReturn(Long saleId, Long itemId, Double returnQty, String remarks) {
        // 1. Validate Input
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        SaleItem item = saleItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (returnQty <= 0 || returnQty > item.getQuantitySold()) {
            throw new RuntimeException("Invalid quantity. Max allowed: " + item.getQuantitySold());
        }

        // Calculate Refund Amount (Price * Qty)
        BigDecimal refundAmount = item.getPricePerUnit().multiply(BigDecimal.valueOf(returnQty));

        // 2. Restore Product Stock
        Product product = item.getProduct();
        product.setTotalStock(product.getTotalStock() + returnQty);
        productRepository.save(product);

        // 3. Update Sale Item (Reduce Qty & Subtotal)
        item.setQuantitySold(item.getQuantitySold() - returnQty);
        item.setSubTotal(item.getSubTotal().subtract(refundAmount));
        saleItemRepository.save(item);

        // 4. Update Main Sale Total
        sale.setTotalAmount(sale.getTotalAmount().subtract(refundAmount));
        saleRepository.save(sale);

        // 5. Financial Adjustment
        // If it was a Credit (Udhaar) sale, reduce the Customer's debt
        if ("Credit".equalsIgnoreCase(sale.getPaymentMode()) && sale.getCustomerPhone() != null) {

            customerRepository.findByPhoneNumber(sale.getCustomerPhone()).ifPresent(customer -> {
                // Reduce Balance (Using BigDecimal math)
                customer.setCurrentBalance(customer.getCurrentBalance().subtract(refundAmount));
                customerRepository.save(customer);

                // Create a "Payment" entry to show this credit in the Ledger
                Payment creditNote = new Payment();
                creditNote.setCustomer(customer);
                creditNote.setPaymentDate(LocalDateTime.now());
                creditNote.setAmount(refundAmount);
                creditNote.setPaymentMode("Return Note"); // This acts as a Credit Note
                creditNote.setRemarks("Returned " + returnQty + " " + product.getUnitType() + " of " + product.getName());
                paymentRepository.save(creditNote);
            });

        } else {
            // If Cash/UPI sale, log it as an Expense (Money going out)
            Expense refund = new Expense();
            refund.setDate(LocalDateTime.now());
            refund.setCategory("Sales Return Refund");
            refund.setDescription("Refund for Bill #" + saleId + ": " + product.getName());
            refund.setAmount(refundAmount);
            expenseRepository.save(refund);
        }
    }
}