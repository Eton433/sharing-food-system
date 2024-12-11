package com.example.sharingfood.repository;

import com.example.sharingfood.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    // 根據姓名模糊搜尋客戶（不區分大小寫）
    List<Customer> findByNameContainingIgnoreCase(String keyword);

    // 根據電子郵件查詢客戶
    Customer findByEmail(String email);

    // 查詢名字為指定值的客戶
    List<Customer> findByName(String name);

    // 根據部分電子郵件查詢客戶
    List<Customer> findByEmailContaining(String partialEmail);
}
