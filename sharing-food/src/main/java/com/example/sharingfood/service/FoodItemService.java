package com.example.sharingfood.service;

import com.example.sharingfood.model.FoodItem;
import com.example.sharingfood.repository.FoodItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;

    public FoodItemService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    // 獲取所有食物項目
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    // 根據描述搜尋食物
    public List<FoodItem> searchFoodItemsByDescription(String keyword) {
        return foodItemRepository.findByDescriptionContainingIgnoreCase(keyword);
    }

    // 根據價格範圍搜尋
    public List<FoodItem> getFoodItemsByPriceRange(double minPrice, double maxPrice) {
        return foodItemRepository.findByPriceBetween(minPrice, maxPrice);
    }

    // 獲取指定賣家的食物項目
    public List<FoodItem> getFoodItemsBySellerStudentId(String studentId) {
        return foodItemRepository.findBySeller_StudentId(studentId);
    }

    // 新增或更新食物項目
    public FoodItem saveFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    // 刪除食物項目
    public void deleteFoodItem(Long id) {
        foodItemRepository.deleteById(id);
    }
}