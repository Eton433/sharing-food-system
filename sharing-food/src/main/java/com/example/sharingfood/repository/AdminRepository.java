package com.example.sharingfood.repository;

import com.example.sharingfood.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
    boolean existsByAdminId(String adminId);

    Admin findByAdminId(String adminId); // 添加方法
}
