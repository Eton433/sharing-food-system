package com.example.sharingfood.controller;

import com.example.sharingfood.model.Customer;
import com.example.sharingfood.model.FoodItem;
import com.example.sharingfood.model.Transaction;
import com.example.sharingfood.repository.CustomerRepository;
import com.example.sharingfood.repository.FoodItemRepository;
import com.example.sharingfood.service.S3Service;
import com.example.sharingfood.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class FoodItemController {

    @Autowired
    private S3Service s3Service;

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

    @GetMapping("/food-items")
    public String listFoodItems(HttpSession session, Model model) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        model.addAttribute("customer", loggedInCustomer);

        List<FoodItem> foodItems = foodItemRepository.findAll();
        model.addAttribute("foodItems", foodItems);
        return "food-items";
    }

    @GetMapping("/food-items/add")
    public String showAddFoodItemPage(HttpSession session, Model model) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login";
        }
        model.addAttribute("customer", loggedInCustomer);
        return "add-food-item";
    }

    @PostMapping("/food-items/add")
    public String addFoodItem(@RequestParam String description,
                              @RequestParam double price,
                              @RequestParam String location,
                              @RequestParam int quantity,
                              @RequestParam String expiryDate,
                              @RequestParam("image") MultipartFile image,
                              HttpSession session,
                              Model model) {
        Customer seller = (Customer) session.getAttribute("loggedInCustomer");
        if (seller == null) {
            return "redirect:/login";
        }

        FoodItem foodItem = new FoodItem();
        foodItem.setDescription(description);
        foodItem.setPrice(price);
        foodItem.setLocation(location);
        foodItem.setQuantity(quantity);
        foodItem.setExpiryDate(expiryDate);

        try {
            if (!image.isEmpty()) {
                String imageUrl = s3Service.uploadFile(image);
                foodItem.setImageUrl(imageUrl);
                System.out.println("Image uploaded successfully. URL: " + imageUrl);
            } else {
                model.addAttribute("errorMessage", "Image file is empty.");
                return "add-food-item";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to upload image: " + e.getMessage());
            return "add-food-item";
        }

        foodItem.setSeller(seller);
        foodItemRepository.save(foodItem);
        return "redirect:/food-items";
    }

    @PostMapping("/food-items/order")
    public String orderFoodItem(@RequestParam Long foodItemId,
                                @RequestParam String studentId,
                                @RequestParam String paymentMethod) {
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
    }
}