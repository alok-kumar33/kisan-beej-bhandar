package com.shop.shopmanagement.service;

import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.entity.Product;
import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.entity.SaleItem;
import com.shop.shopmanagement.repository.CustomerRepository;
import com.shop.shopmanagement.repository.ProductRepository;
import com.shop.shopmanagement.repository.SaleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Sale createSale(List<SaleItem> items, String customerName, String customerPhone, String paymentMode, String soldBy) {

        // 1. VALIDATION FOR CREDIT SALES
        if ("Credit".equalsIgnoreCase(paymentMode) && (customerPhone == null || customerPhone.isEmpty())) {
            throw new RuntimeException("Phone Number is MANDATORY for Credit (Udhaar) Sales!");
        }

        // 2. CREATE SALE HEADER
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setCustomerName(customerName);
        sale.setCustomerPhone(customerPhone);
        sale.setPaymentMode(paymentMode);
        sale.setCreatedBy(soldBy); // Track the Staff/Owner who sold this

        BigDecimal grandTotal = BigDecimal.ZERO;

        // 3. PROCESS ITEMS (Deduct Stock & Calculate Total)
        for (SaleItem item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Check Stock
            if (product.getTotalStock() < item.getQuantitySold()) {
                throw new RuntimeException("Not enough stock for: " + product.getName());
            }

            // Deduct Stock
            product.setTotalStock(product.getTotalStock() - item.getQuantitySold());
            productRepository.save(product);

            // Set Item Details
            // Note: We use the price set in the Controller (which comes from Frontend Wholesale/Retail toggle)
            item.setUnitType(product.getUnitType());
            item.setProduct(product);

            BigDecimal subTotal = item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantitySold()));
            item.setSubTotal(subTotal);

            sale.addItem(item);
            grandTotal = grandTotal.add(subTotal);
        }

        sale.setTotalAmount(grandTotal);

        // 4. HANDLE CREDIT (UDHAAR) LOGIC
        if ("Credit".equalsIgnoreCase(paymentMode)) {
            // Find existing customer or create a placeholder
            Customer customer = customerRepository.findByPhoneNumber(customerPhone)
                    .orElse(new Customer());

            if (customer.getId() == null) {
                // New Customer (Auto-create)
                customer.setName(customerName);
                customer.setPhoneNumber(customerPhone);
                customer.setCustomerType("RETAIL"); // Default
                customer.setCurrentBalance(BigDecimal.ZERO);
            }

            // Add Bill Amount to their Debt
            customer.setCurrentBalance(customer.getCurrentBalance().add(grandTotal));
            customerRepository.save(customer);
        }

        return saleRepository.save(sale);
    }
}