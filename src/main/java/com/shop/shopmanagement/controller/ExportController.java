package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Expense;
import com.shop.shopmanagement.entity.Product;
import com.shop.shopmanagement.entity.Sale;
import com.shop.shopmanagement.repository.ExpenseRepository;
import com.shop.shopmanagement.repository.ProductRepository;
import com.shop.shopmanagement.repository.SaleRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ExportController {

    @Autowired private SaleRepository saleRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ProductRepository productRepository;

    // 1. Show Export Page
    @GetMapping("/export")
    public String showExportPage() {
        return "export";
    }

    // 2. EXPORT SALES (Date Range)
    @GetMapping("/export/sales")
    public void exportSales(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=Sales_Register_" + start + "_to_" + end + ".csv");

        List<Sale> sales = saleRepository.findBySaleDateBetween(start.atStartOfDay(), end.atTime(23, 59, 59));

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Bill No,Date,Customer Name,Phone,Payment Mode,Total Amount,Created By"); // Header

            for (Sale s : sales) {
                writer.println(
                        s.getId() + "," +
                                s.getSaleDate().toLocalDate() + "," +
                                escapeSpecialCharacters(s.getCustomerName()) + "," +
                                (s.getCustomerPhone() != null ? s.getCustomerPhone() : "-") + "," +
                                s.getPaymentMode() + "," +
                                s.getTotalAmount() + "," +
                                (s.getCreatedBy() != null ? s.getCreatedBy() : "Admin")
                );
            }
        }
    }

    // 3. EXPORT EXPENSES (Date Range)
    @GetMapping("/export/expenses")
    public void exportExpenses(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                               @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                               HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=Expenses_" + start + "_to_" + end + ".csv");

        List<Expense> expenses = expenseRepository.findByDateBetweenOrderByDateDesc(start.atStartOfDay(), end.atTime(23, 59, 59));

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Date,Category,Description,Amount"); // Header

            for (Expense e : expenses) {
                writer.println(
                        e.getDate().toLocalDate() + "," +
                                escapeSpecialCharacters(e.getCategory()) + "," +
                                escapeSpecialCharacters(e.getDescription()) + "," +
                                e.getAmount()
                );
            }
        }
    }

    // 4. EXPORT CURRENT INVENTORY (Stock Report)
    @GetMapping("/export/stock")
    public void exportStock(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=Inventory_Stock_Report.csv");

        List<Product> products = productRepository.findAll();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Product ID,Name,Category,Stock Qty,Unit,Cost Price,Retail Price,Total Stock Value"); // Header

            for (Product p : products) {
                double stockValue = p.getTotalStock() * (p.getCostPricePerUnit() != null ? p.getCostPricePerUnit().doubleValue() : 0.0);

                writer.println(
                        p.getId() + "," +
                                escapeSpecialCharacters(p.getName()) + "," +
                                p.getCategory() + "," +
                                p.getTotalStock() + "," +
                                p.getUnitType() + "," +
                                p.getCostPricePerUnit() + "," +
                                p.getRetailPricePerUnit() + "," +
                                String.format("%.2f", stockValue)
                );
            }
        }
    }

    // Helper to prevent CSV breaking if someone types a comma in their name
    private String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}