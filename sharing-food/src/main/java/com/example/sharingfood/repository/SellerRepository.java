package com.example.sharingfood.repository;

import com.example.sharingfood.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    // 根據名稱模糊搜尋賣家，不區分大小寫
    List<Seller> findByNameContainingIgnoreCase(String name);
}
