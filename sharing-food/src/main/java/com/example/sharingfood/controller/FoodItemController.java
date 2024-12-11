package com.example.sharingfood.controller;

import com.example.sharingfood.model.Customer;
import com.example.sharingfood.model.FoodItem;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.repository.CustomerRepository;
import com.example.sharingfood.repository.FoodItemRepository;
import com.example.sharingfood.service.GoogleVisionService;
import com.example.sharingfood.service.S3Service;
import com.example.sharingfood.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class FoodItemController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private GoogleVisionService googleVisionService;

    private final FoodItemRepository foodItemRepository;
    private final TransactionService transactionService;
    private final CustomerRepository customerRepository;

    public FoodItemController(FoodItemRepository foodItemRepository,
                              TransactionService transactionService,
                              CustomerRepository customerRepository) {
        this.foodItemRepository = foodItemRepository;
        this.transactionService = transactionService;
        this.customerRepository = customerRepository;
    }

    /**
     * 確保用戶已登入的方法
     */
    private Customer ensureLoggedIn(HttpSession session) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            throw new IllegalStateException("User not logged in.");
        }
        return loggedInCustomer;
    }

    @GetMapping("/food-items")
    public String listFoodItems(HttpSession session, Model model) {
        try {
            Customer loggedInCustomer = ensureLoggedIn(session);
            model.addAttribute("customer", loggedInCustomer);

            List<FoodItem> foodItems = foodItemRepository.findAll();
            model.addAttribute("foodItems", foodItems);
            return "food-items";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/food-items/add")
    public String showAddFoodItemPage(HttpSession session, Model model) {
        try {
            Customer loggedInCustomer = ensureLoggedIn(session);
            model.addAttribute("customer", loggedInCustomer);
            return "add-food-item";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/food-items/location/{id}")
    public String showFoodLocation(@PathVariable Long id, HttpSession session, Model model) {
        try {
            ensureLoggedIn(session);
            FoodItem foodItem = foodItemRepository.findById(id).orElse(null);

            if (foodItem == null) {
                model.addAttribute("errorMessage", "Food item not found.");
                return "redirect:/food-items";
            }

            // 傳遞食品位置相關資訊
            model.addAttribute("latitude", foodItem.getLatitude());
            model.addAttribute("longitude", foodItem.getLongitude());
            model.addAttribute("description", foodItem.getDescription());
            model.addAttribute("location", foodItem.getLocation()); // 傳遞詳細位置描述

            return "food-location";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/food-items/add")
    public String addFoodItem(@RequestParam String description,
                              @RequestParam String location,
                              @RequestParam double price,
                              @RequestParam int quantity,
                              @RequestParam String expiryDate,
                              @RequestParam("image") MultipartFile image,
                              @RequestParam double latitude,
                              @RequestParam double longitude,
                              HttpSession session,
                              Model model) {
        try {
            Customer seller = ensureLoggedIn(session);

            FoodItem foodItem = new FoodItem();
            foodItem.setDescription(description);
            foodItem.setLocation(location);
            foodItem.setPrice(price);
            foodItem.setQuantity(quantity);
            foodItem.setLatitude(latitude);
            foodItem.setLongitude(longitude);

            try {
                // Step 1: 驗證日期格式和範圍
                LocalDate parsedDate = LocalDate.parse(expiryDate);
                if (parsedDate.isBefore(LocalDate.now()) || parsedDate.isAfter(LocalDate.now().plusMonths(12))) {
                    model.addAttribute("errorMessage", "Invalid expiry date. Please select a date within the next 12 months.");
                    return "add-food-item";
                }
                foodItem.setExpiryDate(parsedDate);

                // Step 2: 檢查圖片是否為空
                if (image.isEmpty()) {
                    model.addAttribute("errorMessage", "Image file is empty.");
                    model.addAttribute("suggestedImage", "/images/default.jpg");
                    return "add-food-item";
                }


                // Step 3: 使用 Google Vision API 審核圖片是否符合描述
                boolean isDescriptionMatched = googleVisionService.checkImageDescription(image, description);
                if (!isDescriptionMatched) {
                    model.addAttribute("errorMessage", "The image does not match the description.");
                    model.addAttribute("suggestedImage", "https://foodsharing-image.s3.ap-southeast-1.amazonaws.com/uploads/alert.jpg");
                    return "add-food-item";
                }
             // Step 4: 上傳圖片到 S3 並獲取圖片 URL
                String imageUrl = s3Service.uploadFile(image);
                foodItem.setImageUrl(imageUrl);

            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Invalid expiry date format.");
                return "add-food-item";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Failed to process the image: " + e.getMessage());
                return "add-food-item";
            }

            // Step 5: 保存食品項目
            foodItem.setSeller(seller);
            foodItemRepository.save(foodItem);
            return "redirect:/food-items";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/food-items/order")
    public String orderFoodItem(@RequestParam Long foodItemId,
                                @RequestParam String studentId,
                                @RequestParam String paymentMethod,
                                HttpSession session) {
        try {
            ensureLoggedIn(session);
            Customer customer = customerRepository.findById(studentId).orElse(null);
            FoodItem foodItem = foodItemRepository.findById(foodItemId).orElse(null);

            if (customer != null && foodItem != null && foodItem.getQuantity() > 0) {
                foodItem.setQuantity(foodItem.getQuantity() - 1);
                foodItemRepository.save(foodItem);

                Transaction transaction = new Transaction();
                transaction.setCustomer(customer);
                transaction.setFoodItem(foodItem);
                transaction.setStatus("Pending");
                transactionService.saveTransaction(transaction);

                return "redirect:/food-items";
            }
            return "redirect:/food-items";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }
}
