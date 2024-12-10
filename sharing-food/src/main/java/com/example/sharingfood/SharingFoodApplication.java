package com.example.sharingfood;

import com.example.sharingfood.model.Customer;
import com.example.sharingfood.model.FoodItem;
import com.example.sharingfood.repository.CustomerRepository;
import com.example.sharingfood.repository.FoodItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SharingFoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharingFoodApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(CustomerRepository customerRepository,
                               FoodItemRepository foodItemRepository) {
        return args -> {
            try {
                // 使用 ObjectMapper 讀取 JSON 檔案，允許帶有註釋的 JSON
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
                InputStream inputStream = getClass().getResourceAsStream("/data.json");

                if (inputStream == null) {
                    System.err.println("無法找到 data.json 檔案，請確保該檔案存在於 resources 資料夾中。");
                    return;
                }

                // 將 JSON 轉換為 Map
                Map<String, Object> data = objectMapper.readValue(inputStream, HashMap.class);

                // 初始化客戶資料
                List<Map<String, String>> customers = (List<Map<String, String>>) data.get("customers");
                for (Map<String, String> customerData : customers) {
                    Customer customer = new Customer();
                    customer.setStudentId(customerData.get("studentId"));
                    customer.setName(customerData.get("name"));
                    customer.setEmail(customerData.get("studentId") + "@nccu.edu.tw");
                    customer.setPassword(customerData.get("password"));
                    customerRepository.save(customer);
                }

                // 初始化食物項目資料
                List<Map<String, Object>> foodItems = (List<Map<String, Object>>) data.get("foodItems");
                for (Map<String, Object> foodItemData : foodItems) {
                    String sellerStudentId = (String) foodItemData.get("sellerStudentId");
                    Customer seller = customerRepository.findById(sellerStudentId).orElse(null);

                    if (seller != null) {
                        FoodItem foodItem = new FoodItem();
                        foodItem.setDescription((String) foodItemData.get("description"));
                        foodItem.setPrice(Double.parseDouble(foodItemData.get("price").toString()));
                        foodItem.setLocation((String) foodItemData.get("location"));
                        foodItem.setQuantity(Integer.parseInt(foodItemData.get("quantity").toString()));

                        // 解析日期並檢查格式
                        try {
                            LocalDate expiryDate = LocalDate.parse((String) foodItemData.get("expiryDate"));
                            foodItem.setExpiryDate(expiryDate);
                        } catch (Exception e) {
                            System.err.println("無法解析日期: " + foodItemData.get("expiryDate") + "，項目將跳過。");
                            continue;
                        }

                        // 新增解析經緯度邏輯
                        try {
                            Double latitude = Double.parseDouble(foodItemData.get("latitude").toString());
                            Double longitude = Double.parseDouble(foodItemData.get("longitude").toString());
                            foodItem.setLatitude(latitude);
                            foodItem.setLongitude(longitude);
                        } catch (Exception e) {
                            System.err.println("經緯度解析失敗: " + e.getMessage());
                            foodItem.setLatitude(25.0272); // 預設為政大校園緯度
                            foodItem.setLongitude(121.5228); // 預設為政大校園經度
                        }

                        foodItem.setImageUrl((String) foodItemData.get("imageUrl"));
                        foodItem.setSeller(seller);
                        foodItemRepository.save(foodItem);
                    } else {
                        System.err.println("無法找到賣家: " + sellerStudentId + "，食物項目將不會被儲存。");
                    }
                }

                // 輸出初始化的資料
                System.out.println("初始化完成！");
                System.out.println("\n--- 客戶資料 ---");
                customerRepository.findAll().forEach(System.out::println);

                System.out.println("\n--- 食物項目資料 ---");
                foodItemRepository.findAll().forEach(System.out::println);

            } catch (Exception e) {
                System.err.println("初始化資料時發生錯誤：" + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
