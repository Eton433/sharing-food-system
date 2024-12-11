package com.example.sharingfood.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class GoogleVisionService {

    @Value("${google.vision.api.key}")
    private String apiKey;

    // 檢查圖片描述是否匹配
    public boolean checkImageDescription(MultipartFile image, String description) throws IOException {
        // Step 1: 圖片基礎檢查
        if (!isValidImage(image)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        // Step 2: 轉換為 Base64 編碼
        String imageBase64 = processImage(image);

        // Step 3: 調用 Google Vision API
        String jsonResponse = analyzeImageWithGoogleVision(imageBase64);

        // Step 4: 分析標籤並匹配描述
        return analyzeLabels(jsonResponse, description);
    }

    // 基礎圖片檢查（大小、格式等）
    private boolean isValidImage(MultipartFile file) {
        return !file.isEmpty() && file.getSize() <= 5 * 1024 * 1024; // 限制大小 5MB
    }

    // 圖片轉換為 Base64 編碼
    private String processImage(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    // 使用 RestTemplate 調用 Google Vision API
    private String analyzeImageWithGoogleVision(String imageBase64) {
        String apiUrl = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

        // 構建請求體
        String requestBody = "{\n" +
                "  \"requests\": [\n" +
                "    {\n" +
                "      \"image\": {\n" +
                "        \"content\": \"" + imageBase64 + "\"\n" +
                "      },\n" +
                "      \"features\": [\n" +
                "        {\n" +
                "          \"type\": \"LABEL_DETECTION\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // 使用 RestTemplate 發送請求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
        return response.getBody();
    }

    // 分析返回的 JSON 結果
    private boolean analyzeLabels(String jsonResponse, String description) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode labels = root.path("responses").path(0).path("labelAnnotations");

        // 設置匹配閾值
        double threshold = 0.8;

        for (JsonNode label : labels) {
            String labelDescription = label.path("description").asText().toLowerCase();
            double score = label.path("score").asDouble();

            // 匹配描述和閾值
            if (labelDescription.contains(description.toLowerCase()) && score >= threshold) {
                return true;
            }
        }
        return false;
    }
}
