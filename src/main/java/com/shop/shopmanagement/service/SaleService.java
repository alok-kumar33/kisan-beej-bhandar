package com.shop.shopmanagement.service;

import com.shop.shopmanagement.entity.Customer;
import com.shop.shopmanagement.entity.Expense;
import com.shop.shopmanagement.entity.Product;
import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.entity.SaleItem;
import com.shop.shopmanagement.repository.CustomerRepository;
import com.shop.shopmanagement.repository.ExpenseRepository; // NEW IMPORT
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

    @Autowired
    private ExpenseRepository expenseRepository; // INJECTED EXPENSE REPO

    @Transactional
    public Sale createSale(List<SaleItem> items, String customerName, String customerPhone, String paymentMode, String soldBy, BigDecimal discount) {

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
        sale.setCreatedBy(soldBy);

        // Set the discount (Safeguard against nulls)
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
        sale.setDiscount(discount);

        BigDecimal subTotalAmount = BigDecimal.ZERO;

        // 3. PROCESS ITEMS (Deduct Stock & Calculate Total)
        for (SaleItem item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getTotalStock() < item.getQuantitySold()) {
                throw new RuntimeException("Not enough stock for: " + product.getName());
            }

            product.setTotalStock(product.getTotalStock() - item.getQuantitySold());
            productRepository.save(product);

            item.setUnitType(product.getUnitType());
            item.setProduct(product);

            BigDecimal lineTotal = item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantitySold()));
            item.setSubTotal(lineTotal);

            sale.addItem(item);
            subTotalAmount = subTotalAmount.add(lineTotal);
        }

        // 4. CALCULATE FINAL DISCOUNTED AMOUNT
        BigDecimal finalAmount = subTotalAmount.subtract(discount);
        sale.setTotalAmount(finalAmount);

        // 5. AUTO-CREATE EXPENSE RECORD FOR THE DISCOUNT
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            Expense expense = new Expense();
            expense.setDate(LocalDateTime.now());
            expense.setCategory("Customer Discount");
            expense.setAmount(discount);

            String customerLabel = (customerName != null && !customerName.isEmpty()) ? customerName : "Walk-in Customer";
            expense.setDescription("Discount given on sale to " + customerLabel + " by " + soldBy);

            expenseRepository.save(expense);
        }

        // 6. HANDLE CREDIT (UDHAAR) LOGIC
        if ("Credit".equalsIgnoreCase(paymentMode)) {
            Customer customer = customerRepository.findByPhoneNumber(customerPhone)
                    .orElse(new Customer());

            if (customer.getId() == null) {
                customer.setName(customerName);
                customer.setPhoneNumber(customerPhone);
                customer.setCustomerType("RETAIL");
                customer.setCurrentBalance(BigDecimal.ZERO);
            }

            // Add FINAL DISCOUNTED Bill Amount to their Debt
            customer.setCurrentBalance(customer.getCurrentBalance().add(finalAmount));
            customerRepository.save(customer);
        }

        return saleRepository.save(sale);
    }
}