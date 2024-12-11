package com.example.sharingfood.service;

import com.example.sharingfood.model.Seller;
import com.example.sharingfood.repository.SellerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    // 獲取所有賣家
    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    // 根據 ID 查詢賣家
    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id).orElse(null);
    }

    // 新增或更新賣家
    public Seller saveSeller(Seller seller) {
        return sellerRepository.save(seller);
    }

    // 刪除賣家
    public void deleteSeller(Long id) {
        sellerRepository.deleteById(id);
    }
}
