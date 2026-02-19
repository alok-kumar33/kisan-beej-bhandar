package com.shop.shopmanagement.service;

import com.shop.shopmanagement.dto.DayBookEntry;
import com.shop.shopmanagement.dto.ItemReportEntry;
import com.shop.shopmanagement.entity.*;
import com.shop.shopmanagement.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SupplierPaymentRepository supplierPaymentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // --- MISSING METHOD RESTORED ---
    public List<Product> getLowStockItems() {
        return productRepository.findByTotalStockLessThan(10.0);
    }

    // --- 1. MAIN DASHBOARD REPORT ---
    public Map<String, Object> getReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCOGS = BigDecimal.ZERO;
        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalUPI = BigDecimal.ZERO;

        for (Sale sale : sales) {
            totalRevenue = totalRevenue.add(sale.getTotalAmount());

            if ("Cash".equalsIgnoreCase(sale.getPaymentMode())) {
                totalCash = totalCash.add(sale.getTotalAmount());
            } else if ("Credit".equalsIgnoreCase(sale.getPaymentMode())) {
                totalCredit = totalCredit.add(sale.getTotalAmount());
            } else {
                totalUPI = totalUPI.add(sale.getTotalAmount());
            }

            for (SaleItem item : sale.getItems()) {
                BigDecimal costPrice = item.getProduct().getCostPricePerUnit() != null ?
                        item.getProduct().getCostPricePerUnit() : BigDecimal.ZERO;

                BigDecimal itemCost = costPrice.multiply(BigDecimal.valueOf(item.getQuantitySold()));
                totalCOGS = totalCOGS.add(itemCost);
            }
        }

        BigDecimal grossProfit = totalRevenue.subtract(totalCOGS);

        List<Expense> expenses = expenseRepository.findByDateBetweenOrderByDateDesc(start, end);
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for(Expense e : expenses) {
            totalExpenses = totalExpenses.add(e.getAmount());
        }

        BigDecimal netProfit = grossProfit.subtract(totalExpenses);

        // Chart Data
        Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyTotals.put(current, BigDecimal.ZERO);
            current = current.plusDays(1);
        }
        for (Sale sale : sales) {
            LocalDate date = sale.getSaleDate().toLocalDate();
            dailyTotals.put(date, dailyTotals.get(date).add(sale.getTotalAmount()));
        }

        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();
        dailyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    chartLabels.add(entry.getKey().format(DateTimeFormatter.ofPattern("dd-MMM")));
                    chartData.add(entry.getValue());
                });

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("sales", sales);
        report.put("totalRevenue", totalRevenue);
        report.put("grossProfit", grossProfit);
        report.put("totalExpenses", totalExpenses);
        report.put("totalProfit", netProfit);
        report.put("splitCash", totalCash);
        report.put("splitCredit", totalCredit);
        report.put("splitUPI", totalUPI);
        report.put("chartLabels", chartLabels);
        report.put("chartData", chartData);

        return report;
    }

    // --- 2. DAY BOOK ---
    public List<DayBookEntry> getDayBook(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<DayBookEntry> book = new ArrayList<>();

        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);
        for(Sale s : sales) {
            BigDecimal amtIn = "Credit".equalsIgnoreCase(s.getPaymentMode()) ? BigDecimal.ZERO : s.getTotalAmount();
            book.add(new DayBookEntry(s.getSaleDate(), "SALE",
                    "Bill #" + s.getId() + " - " + s.getCustomerName(),
                    amtIn, BigDecimal.ZERO, s.getPaymentMode()));
        }

        List<Expense> expenses = expenseRepository.findByDateBetweenOrderByDateDesc(start, end);
        for(Expense e : expenses) {
            book.add(new DayBookEntry(e.getDate(), "EXPENSE",
                    e.getCategory() + ": " + e.getDescription(),
                    BigDecimal.ZERO, e.getAmount(), "Cash"));
        }

        List<Payment> received = paymentRepository.findByPaymentDateBetween(start, end);
        for(Payment p : received) {
            book.add(new DayBookEntry(p.getPaymentDate(), "RECEIPT",
                    "Recv from: " + p.getCustomer().getName(),
                    p.getAmount(), BigDecimal.ZERO, p.getPaymentMode()));
        }

        List<SupplierPayment> paid = supplierPaymentRepository.findByPaymentDateBetween(start, end);
        for(SupplierPayment sp : paid) {
            book.add(new DayBookEntry(sp.getPaymentDate(), "PAYMENT",
                    "Paid to: " + sp.getSupplier().getAgencyName(),
                    BigDecimal.ZERO, sp.getAmount(), "Cash"));
        }

        Collections.sort(book);
        return book;
    }

    // --- 3. ITEM PERFORMANCE (UPDATED) ---
    public List<ItemReportEntry> getItemReport(LocalDate start, LocalDate end) {
        // Updated Query to fetch UnitType
        String hql = "SELECT new com.shop.shopmanagement.dto.ItemReportEntry(" +
                "   p.name, " +
                "   SUM(si.quantitySold), " +
                "   SUM(si.quantitySold * si.pricePerUnit), " +
                "   p.unitType" +
                ") " +
                "FROM SaleItem si " +
                "JOIN si.sale s " +
                "JOIN si.product p " +
                "WHERE s.saleDate BETWEEN :start AND :end " +
                "GROUP BY p.name, p.unitType " +
                "ORDER BY SUM(si.quantitySold * si.pricePerUnit) DESC";

        return entityManager.createQuery(hql, ItemReportEntry.class)
                .setParameter("start", start.atStartOfDay())
                .setParameter("end", end.atTime(23, 59, 59))
                .getResultList();
    }

    // --- 4. STOCK SUMMARY ---
    public Map<String, Object> getStockSummary() {
        List<Product> products = productRepository.findAll();
        BigDecimal totalStockValue = BigDecimal.ZERO;
        BigDecimal totalPotentialRevenue = BigDecimal.ZERO;

        for(Product p : products) {
            BigDecimal cost = p.getCostPricePerUnit() != null ? p.getCostPricePerUnit() : BigDecimal.ZERO;
            BigDecimal price = p.getRetailPricePerUnit() != null ? p.getRetailPricePerUnit() : BigDecimal.ZERO;

            BigDecimal stockVal = cost.multiply(BigDecimal.valueOf(p.getTotalStock()));
            BigDecimal saleVal = price.multiply(BigDecimal.valueOf(p.getTotalStock()));

            totalStockValue = totalStockValue.add(stockVal);
            totalPotentialRevenue = totalPotentialRevenue.add(saleVal);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("products", products);
        summary.put("inventoryValue", totalStockValue);
        summary.put("salesValue", totalPotentialRevenue);
        return summary;
    }
}