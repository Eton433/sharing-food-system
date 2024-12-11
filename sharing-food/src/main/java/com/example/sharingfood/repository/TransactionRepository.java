package com.example.sharingfood.repository;

import com.example.sharingfood.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerStudentId(String studentId);

    List<Transaction> findByFoodItem_Seller_StudentId(String sellerId);
}
