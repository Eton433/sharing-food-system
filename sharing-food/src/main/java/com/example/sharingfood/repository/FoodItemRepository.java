package com.example.sharingfood.repository;

import com.example.sharingfood.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // 根据描述模糊搜索食物
    List<FoodItem> findByDescriptionContainingIgnoreCase(String keyword);

    // 根据价格范围搜索食物项目
    List<FoodItem> findByPriceBetween(double minPrice, double maxPrice);

    // 根据卖家的学号搜索食物项目
    List<FoodItem> findBySeller_StudentId(String studentId);
}
