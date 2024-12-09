package com.example.sharingfood.controller;

import com.example.sharingfood.model.Customer;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.service.TransactionService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // 顧客查看自己的交易記錄
    @GetMapping("/transactions/customer")
    public String viewCustomerTransactions(HttpSession session, Model model) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login";
        }

        List<Transaction> transactions = transactionService.getTransactionsByCustomer(loggedInCustomer.getStudentId());
        model.addAttribute("transactions", transactions);
        model.addAttribute("customerName", loggedInCustomer.getName());
        return "customer-transactions";
    }

    // 賣家查看自己的交易記錄
    @GetMapping("/transactions/seller")
    public String viewSellerTransactions(HttpSession session, Model model) {
        Customer seller = (Customer) session.getAttribute("loggedInCustomer");
        if (seller == null) {
            return "redirect:/login";
        }

        List<Transaction> transactions = transactionService.getTransactionsBySeller(seller.getStudentId());
        model.addAttribute("transactions", transactions);
        model.addAttribute("sellerName", seller.getName());
        return "seller-transactions";
    }

    // 賣家更新交易狀態
    @PostMapping("/transactions/update")
    public String updateTransactionStatus(@RequestParam Long transactionId,
                                          @RequestParam String status,
                                          HttpSession session) {
        Customer seller = (Customer) session.getAttribute("loggedInCustomer");
        if (seller == null) {
            return "redirect:/login";
        }

        transactionService.updateTransactionStatusForSeller(transactionId, status);
        return "redirect:/transactions/seller";
    }

    // 管理員查看所有交易
    @GetMapping("/transactions/admin")
    public String viewAllTransactions(Model model) {
        List<Transaction> transactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        return "admin-transactions";
    }

    // 管理員更新交易狀態
    @PostMapping("/transactions/admin/update")
    public String adminUpdateTransactionStatus(@RequestParam Long transactionId,
                                               @RequestParam String status) {
        transactionService.updateTransactionStatus(transactionId, status);
        return "redirect:/transactions/admin";
    }
}
