package com.example.sharingfood.controller;

import com.example.sharingfood.model.Seller;
import com.example.sharingfood.repository.SellerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SellerController {

    private final SellerRepository sellerRepository;

    public SellerController(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @GetMapping("/sellers")
    public String listSellers(Model model) {
        List<Seller> sellers = sellerRepository.findAll(); // 查詢所有賣家
        model.addAttribute("sellers", sellers);
        return "sellers"; // 對應 src/main/resources/templates/sellers.html
    }
}
