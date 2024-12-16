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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.imageio.ImageIO;

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

            model.addAttribute("latitude", foodItem.getLatitude());
            model.addAttribute("longitude", foodItem.getLongitude());
            model.addAttribute("description", foodItem.getDescription());
            model.addAttribute("location", foodItem.getLocation());

            return "food-location";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/food-items/add")
    public String addFoodItem(
            @RequestParam String description,
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
            // Step 0: 確認用戶是否登入
            Customer seller = ensureLoggedIn(session);

            // Step 1: 初始化 FoodItem 實體
            FoodItem foodItem = new FoodItem();
            foodItem.setDescription(description);
            foodItem.setLocation(location);
            foodItem.setPrice(price);
            foodItem.setQuantity(quantity);
            foodItem.setLatitude(latitude);
            foodItem.setLongitude(longitude);

            try {
                // Step 2: 驗證過期日期
                LocalDate parsedDate = LocalDate.parse(expiryDate);
                if (parsedDate.isBefore(LocalDate.now()) || parsedDate.isAfter(LocalDate.now().plusMonths(12))) {
                    model.addAttribute("errorMessage", "Invalid expiry date. Please select a date within the next 12 months.");
                    return "add-food-item";
                }
                foodItem.setExpiryDate(parsedDate);

                // Step 3: 檢查圖片
                if (image.isEmpty()) {
                    model.addAttribute("errorMessage", "Image file is empty.");
                    model.addAttribute("suggestedImage", "/images/default.jpg");
                    return "add-food-item";
                }

                if (!image.getContentType().startsWith("image/")) {
                    throw new IllegalArgumentException("Uploaded file is not a valid image.");
                }

                // Step 4: 強制轉換圖片為 JPG 並處理尺寸
                BufferedImage originalImage = ImageIO.read(image.getInputStream());
                if (originalImage == null) {
                    throw new IllegalArgumentException("Unable to process the uploaded image. Please upload a valid image file.");
                }

                int minWidth = 640, minHeight = 480;
                int maxWidth = 1600, maxHeight = 1200;

                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();

                BufferedImage adjustedImage = originalImage;

                if (originalWidth < minWidth || originalHeight < minHeight || originalWidth > maxWidth || originalHeight > maxHeight) {
                    int newWidth = Math.min(Math.max(originalWidth, minWidth), maxWidth);
                    int newHeight = Math.min(Math.max(originalHeight, minHeight), maxHeight);

                    adjustedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = adjustedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(adjustedImage, "jpg", os);
                byte[] imageBytes = os.toByteArray();

                // Step 5: 使用 Google Vision API 審核圖片
                boolean isDescriptionMatched = googleVisionService.checkImageDescription(imageBytes, description, "image/jpeg");
                if (!isDescriptionMatched) {
                    model.addAttribute("errorMessage", "The image does not match the description.");
                    model.addAttribute("suggestedImage", "https://foodsharing-image.s3.ap-southeast-1.amazonaws.com/uploads/alert.jpg");
                    return "add-food-item";
                }

                // Step 6: 上傳圖片到 S3 並獲取 URL
                String uniqueFileName = "image_" + System.currentTimeMillis() + ".jpg";
                String imageUrl = s3Service.uploadFile(imageBytes, uniqueFileName, "image/jpeg");
                foodItem.setImageUrl(imageUrl);

            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Invalid expiry date format.");
                return "add-food-item";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Failed to process the image: " + e.getMessage());
                return "add-food-item";
            }

            // Step 7: 保存食品項目
            foodItem.setSeller(seller);
            foodItemRepository.save(foodItem);

            // 成功保存後重定向
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
