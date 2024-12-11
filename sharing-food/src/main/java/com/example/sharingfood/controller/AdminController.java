package com.example.sharingfood.controller;

import com.example.sharingfood.model.Admin;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.service.AdminService;
import com.example.sharingfood.service.TransactionService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // 用於 WebSocket 消息推送

    /**
     * 管理員登入頁面
     */
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") != null) {
            return "redirect:/admin/transactions";
        }
        return "admin-login";
    }

    /**
     * 管理員處理登入
     */
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        Admin admin = adminService.findByAdminId(username);
        if (admin != null && admin.getPassword().equals(password)) {
            session.setAttribute("loggedInAdmin", admin);
            return "redirect:/admin/transactions";
        }
        model.addAttribute("error", "Invalid username or password.");
        return "admin-login";
    }

    /**
     * 查看所有交易
     */
    @GetMapping("/transactions")
    public String viewAllTransactions(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        List<Transaction> transactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        return "admin-transactions";
    }

    /**
     * 管理員更新交易狀態並推送通知
     */
    @PostMapping("/transactions/update")
    public String updateTransactionStatus(@RequestParam Long transactionId,
                                          @RequestParam String status,
                                          HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        // 更新交易狀態
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction != null) {
            String oldStatus = transaction.getStatus();

            // 更新狀態
            transactionService.updateTransactionStatus(transactionId, status);

            // 只有在狀態更改時才發送通知
            if (!oldStatus.equals(status)) {
                sendNotifications(transaction, status);
            }
        }

        return "redirect:/admin/transactions";
    }

    /**
     * 發送通知給買家和賣家
     */
    private void sendNotifications(Transaction transaction, String status) {
        // 通知賣家
        String sellerTopic = "/topic/seller/" + transaction.getFoodItem().getSeller().getStudentId();
        String sellerMessage = "Transaction " + transaction.getId() + " updated to " + status;
        messagingTemplate.convertAndSend(sellerTopic, sellerMessage);

        // 通知買家
        String buyerTopic = "/topic/buyer/" + transaction.getCustomer().getStudentId();
        String buyerMessage = "Your transaction " + transaction.getId() + " has been updated to " + status;
        messagingTemplate.convertAndSend(buyerTopic, buyerMessage);
    }

    /**
     * 管理員登出
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    /**
     * 檢查管理員是否已登入
     */
    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInAdmin") != null;
    }
}
