package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class AdvancedReportController {

    @Autowired
    private ReportService reportService;

    // 1. DAY BOOK (Master Log)
    @GetMapping("/reports/daybook")
    public String showDayBook(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        if(date == null) date = LocalDate.now();
        model.addAttribute("date", date);
        model.addAttribute("entries", reportService.getDayBook(date));
        return "report-daybook";
    }

    // 2. ITEM PERFORMANCE
    @GetMapping("/reports/items")
    public String showItemReport(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end, Model model) {
        if(start == null) start = LocalDate.now().minusDays(30);
        if(end == null) end = LocalDate.now();

        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("items", reportService.getItemReport(start, end));
        return "report-items";
    }

    // 3. STOCK VALUATION
    @GetMapping("/reports/stock")
    public String showStockReport(Model model) {
        model.addAllAttributes(reportService.getStockSummary());
        return "report-stock";
    }
}