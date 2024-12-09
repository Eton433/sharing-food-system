package com.example.sharingfood.controller;

import com.example.sharingfood.model.Customer;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final TransactionService transactionService;

    public CustomerController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // 賣家查看自己的交易
    @GetMapping("/transactions/seller")
    public String viewSellerTransactions(HttpSession session, Model model) {
        Customer seller = (Customer) session.getAttribute("loggedInCustomer");
        if (seller == null) {
            return "redirect:/login";
        }

        // 調用服務層獲取賣家相關交易
        List<Transaction> transactions = transactionService.getTransactionsBySeller(seller.getStudentId());
        model.addAttribute("transactions", transactions);
        return "seller-transactions";
    }
}
