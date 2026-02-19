package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.repository.SaleRepository;
import com.shop.shopmanagement.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ReturnController {

    @Autowired private SaleRepository saleRepository;
    @Autowired private ReturnService returnService;

    // 1. Show Search Page
    @GetMapping("/returns")
    public String showReturnPage() {
        return "return-search";
    }

    // 2. Find Bill
    @PostMapping("/returns/find")
    public String findBillForReturn(@RequestParam("billId") Long billId, Model model) {
        Optional<Sale> sale = saleRepository.findById(billId);
        if (sale.isPresent()) {
            model.addAttribute("sale", sale.get());
            return "return-process";
        } else {
            model.addAttribute("error", "Bill Number " + billId + " not found!");
            return "return-search";
        }
    }

    // 3. Process the Return
    @PostMapping("/returns/process")
    public String processReturn(@RequestParam("saleId") Long saleId,
                                @RequestParam("itemId") Long itemId,
                                @RequestParam("returnQty") Double returnQty,
                                @RequestParam("remarks") String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            returnService.processReturn(saleId, itemId, returnQty, remarks);
            redirectAttributes.addFlashAttribute("success", "✅ Item returned successfully! Stock & Ledger updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }
        return "redirect:/returns";
    }
}