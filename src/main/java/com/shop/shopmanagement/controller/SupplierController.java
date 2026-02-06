package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Supplier;
import com.shop.shopmanagement.repository.PurchaseRepository;
import com.shop.shopmanagement.repository.SupplierRepository;
import com.shop.shopmanagement.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseService purchaseService;

    // 1. List All Suppliers
    @GetMapping("/suppliers")
    public String listSuppliers(Model model) {
        model.addAttribute("suppliers", supplierRepository.findAll());
        return "suppliers"; // Looks for suppliers.html
    }

    // 2. Add New Supplier
    @PostMapping("/suppliers/add")
    public String addSupplier(Supplier supplier) {
        purchaseService.saveSupplier(supplier);
        return "redirect:/suppliers";
    }

    // 3. View Supplier Ledger (Unified)
    @GetMapping("/suppliers/{id}")
    public String viewSupplier(@PathVariable Long id, Model model) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        model.addAttribute("supplier", s);

        // NEW: Get Unified Ledger (Purchases + Payments)
        model.addAttribute("ledgerEntries", purchaseService.getSupplierLedger(id));

        return "supplier-view"; // Looks for supplier-view.html
    }

    // 4. Record a New Purchase Bill
    @PostMapping("/suppliers/purchase")
    public String addPurchase(@RequestParam Long supplierId,
                              @RequestParam String billNo,
                              @RequestParam BigDecimal amount,
                              @RequestParam String mode) {

        purchaseService.recordPurchase(supplierId, billNo, amount, mode);
        return "redirect:/suppliers/" + supplierId;
    }

    // 5. Pay Supplier (Clear Debt)
    @PostMapping("/suppliers/pay")
    public String paySupplier(@RequestParam Long supplierId,
                              @RequestParam BigDecimal amount) {

        purchaseService.paySupplier(supplierId, amount);
        return "redirect:/suppliers/" + supplierId;
    }
}