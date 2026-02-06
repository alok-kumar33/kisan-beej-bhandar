package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Expense;
import com.shop.shopmanagement.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/expenses")
    public String showExpenses(Model model) {
        // Show last 30 days of expenses by default in the list
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        model.addAttribute("expenses", expenseRepository.findByDateBetweenOrderByDateDesc(thirtyDaysAgo, LocalDateTime.now()));
        return "expenses"; // Loads expenses.html
    }

    @PostMapping("/expenses/save")
    public String saveExpense(@RequestParam String category,
                              @RequestParam BigDecimal amount,
                              @RequestParam String description) {

        Expense expense = new Expense();
        expense.setDate(LocalDateTime.now());
        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setDescription(description);

        expenseRepository.save(expense);

        return "redirect:/expenses";
    }

    // Add a delete feature just in case of a mistake
    @GetMapping("/expenses/delete")
    public String deleteExpense(@RequestParam Long id) {
        expenseRepository.deleteById(id);
        return "redirect:/expenses";
    }
}