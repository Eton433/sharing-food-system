package com.example.sharingfood.service;

import com.example.sharingfood.model.FoodItem;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.repository.TransactionRepository;
import com.example.sharingfood.repository.FoodItemRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FoodItemRepository foodItemRepository;

    public TransactionService(TransactionRepository transactionRepository, FoodItemRepository foodItemRepository) {
        this.transactionRepository = transactionRepository;
        this.foodItemRepository = foodItemRepository;
    }

    // 管理員查看所有交易
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // 顧客查看自己的交易
    public List<Transaction> getTransactionsByCustomer(String studentId) {
        return transactionRepository.findByCustomerStudentId(studentId);
    }

    // 賣家查看自己的交易
    public List<Transaction> getTransactionsBySeller(String sellerId) {
        return transactionRepository.findByFoodItem_Seller_StudentId(sellerId);
    }
    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    // 更新交易狀態（管理員功能）
    public void updateTransactionStatus(Long transactionId, String status) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction != null && !transaction.getStatus().equals(status)) {
            // 如果是取消交易，恢復食物數量
            if ("Cancelled".equalsIgnoreCase(status)) {
                FoodItem foodItem = transaction.getFoodItem();
                if (foodItem != null) {
                    foodItem.setQuantity(foodItem.getQuantity() + 1);
                    foodItemRepository.save(foodItem);
                }
            }
            transaction.setStatus(status);
            transactionRepository.save(transaction);
        }
    }

    // 賣家更新交易狀態
    public void updateTransactionStatusForSeller(Long transactionId, String status) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction != null && !transaction.getStatus().equals(status)) {
            // 取消交易時恢復食物數量
            if ("Cancelled".equalsIgnoreCase(status)) {
                FoodItem foodItem = transaction.getFoodItem();
                if (foodItem != null) {
                    foodItem.setQuantity(foodItem.getQuantity() + 1);
                    foodItemRepository.save(foodItem);
                }
            }
            transaction.setStatus(status);
            transactionRepository.save(transaction);
        }
    }

    // 保存交易記錄
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}