package com.example.sharingfood.controller;

import com.example.sharingfood.model.Admin;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.service.AdminService;
import com.example.sharingfood.service.TransactionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/transactions")
    public String viewAllTransactions(Model model) {
        List<Transaction> transactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        return "admin-transactions"; // 對應 `admin-transactions.html`
    }

    @GetMapping("/login")
    public String loginForm() {
        return "admin-login"; // 对应 `admin-login.html`
    }
    @PostMapping("/transactions/update")
    public String updateTransactionStatus(@RequestParam Long transactionId, @RequestParam String status) {
        transactionService.updateTransactionStatus(transactionId, status);
        return "redirect:/admin/transactions";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        Admin admin = adminService.findByAdminId(username);
        if (admin != null && admin.getPassword().equals(password)) {
            model.addAttribute("admin", admin);
            return "admin-dashboard"; // 对应 `admin-dashboard.html`
        } else {
            model.addAttribute("error", "Invalid username or password.");
            return "admin-login";
        }
    }
}
