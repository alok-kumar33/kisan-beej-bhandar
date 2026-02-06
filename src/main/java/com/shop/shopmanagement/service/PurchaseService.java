package com.shop.shopmanagement.service;

import com.shop.shopmanagement.dto.LedgerEntry;
import com.shop.shopmanagement.entity.Purchase;
import com.shop.shopmanagement.entity.Supplier;
import com.shop.shopmanagement.entity.SupplierPayment;
import com.shop.shopmanagement.repository.PurchaseRepository;
import com.shop.shopmanagement.repository.SupplierPaymentRepository;
import com.shop.shopmanagement.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private SupplierPaymentRepository supplierPaymentRepository;

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void recordPurchase(Long supplierId, String billNo, BigDecimal amount, String mode) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setBillNumber(billNo);
        purchase.setTotalAmount(amount);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPaymentMode(mode);
        purchase.setStatus("Credit".equalsIgnoreCase(mode) ? "PENDING" : "PAID");

        purchaseRepository.save(purchase);

        if ("Credit".equalsIgnoreCase(mode)) {
            supplier.setCurrentBalance(supplier.getCurrentBalance().add(amount));
            supplierRepository.save(supplier);
        }
    }

    @Transactional
    public void paySupplier(Long supplierId, BigDecimal amount) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        // 1. Reduce Debt
        supplier.setCurrentBalance(supplier.getCurrentBalance().subtract(amount));
        supplierRepository.save(supplier);

        // 2. SAVE HISTORY (New!)
        SupplierPayment pay = new SupplierPayment();
        pay.setSupplier(supplier);
        pay.setAmount(amount);
        pay.setPaymentDate(LocalDateTime.now());
        pay.setRemarks("Payment Out");
        supplierPaymentRepository.save(pay);
    }

    // NEW: Get Unified Supplier Ledger
    public List<LedgerEntry> getSupplierLedger(Long supplierId) {
        List<LedgerEntry> ledger = new ArrayList<>();

        // A. Purchases (CREDIT - We Owe Them)
        List<Purchase> purchases = purchaseRepository.findBySupplierIdOrderByPurchaseDateDesc(supplierId);
        for(Purchase p : purchases) {
            if("Credit".equalsIgnoreCase(p.getPaymentMode())) {
                ledger.add(new LedgerEntry(
                        p.getPurchaseDate(),
                        "Purchase Bill: " + p.getBillNumber(),
                        "CREDIT", // In Supplier accounts, Purchase is Credit (Liability increases)
                        p.getTotalAmount()
                ));
            }
        }

        // B. Payments (DEBIT - We Paid Them)
        List<SupplierPayment> payments = supplierPaymentRepository.findBySupplierId(supplierId);
        for(SupplierPayment sp : payments) {
            ledger.add(new LedgerEntry(
                    sp.getPaymentDate(),
                    "Paid to Supplier",
                    "DEBIT", // Liability decreases
                    sp.getAmount()
            ));
        }

        Collections.sort(ledger);
        return ledger;
    }
}