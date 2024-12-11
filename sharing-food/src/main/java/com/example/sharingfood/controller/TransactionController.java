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
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 顧客查看自己的交易記錄
     */
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

    /**
     * 賣家查看自己的交易記錄
     */
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

    /**
     * 賣家更新交易狀態
     */
    @PostMapping("/transactions/update")
    public String updateTransactionStatus(@RequestParam Long transactionId,
                                          @RequestParam String status,
                                          HttpSession session,
                                          Model model) {
        Customer seller = (Customer) session.getAttribute("loggedInCustomer");
        if (seller == null) {
            return "redirect:/login";
        }

        // 獲取交易的當前狀態
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            model.addAttribute("errorMessage", "Transaction not found.");
            return "redirect:/transactions/seller";
        }

        // 如果交易已被管理員更新，阻止操作
        if ("Completed".equals(transaction.getStatus()) || "Cancelled".equals(transaction.getStatus())) {
            model.addAttribute("errorMessage", "This transaction has been updated by the admin. You cannot modify it.");
            return "redirect:/transactions/seller";
        }

        // 更新交易狀態
        transactionService.updateTransactionStatusForSeller(transactionId, status);
        return "redirect:/transactions/seller";
    }
    
    @PostMapping("/transactions/update/ajax")
    @ResponseBody
    public Map<String, Object> updateTransactionStatusAjax(@RequestParam Long transactionId,
                                                           @RequestParam String status) {
        // 更新交易狀態
        transactionService.updateTransactionStatus(transactionId, status);

        // 返回更新後的交易數據和相關的食物數量
        Transaction transaction = transactionService.getTransactionById(transactionId);
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("status", status);
        response.put("foodItemId", transaction.getFoodItem().getId());
        response.put("updatedQuantity", transaction.getFoodItem().getQuantity());

        return response;
    }

    /**
     * 管理員查看所有交易
     */
    @GetMapping("/transactions/admin")
    public String viewAllTransactions(HttpSession session, Model model) {
        // 檢查管理員是否已登入
        if (!isAdminLoggedIn(session)) {
            return "redirect:/login";
        }

        List<Transaction> transactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        return "admin-transactions";
    }

    /**
     * 管理員更新交易狀態
     */
    @PostMapping("/transactions/admin/update")
    public String adminUpdateTransactionStatus(@RequestParam Long transactionId,
                                               @RequestParam String status,
                                               HttpSession session) {
        // 檢查管理員是否已登入
        if (!isAdminLoggedIn(session)) {
            return "redirect:/login";
        }

        // 更新交易狀態
        transactionService.updateTransactionStatus(transactionId, status);
        return "redirect:/transactions/admin";
    }

    /**
     * 檢查管理員是否已登入的方法
     */
    private boolean isAdminLoggedIn(HttpSession session) {
        Object loggedInAdmin = session.getAttribute("loggedInAdmin");
        return loggedInAdmin != null;
    }
}
