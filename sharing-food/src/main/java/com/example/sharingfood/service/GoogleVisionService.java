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

import java.util.Base64;

@Service
public class GoogleVisionService {

    @Value("${google.vision.api.key}")
    private String apiKey;

    public boolean checkImageDescription(byte[] imageBytes, String description, String contentType) {
        try {
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

            String jsonResponse = analyzeImageWithGoogleVision(imageBase64);

            return analyzeLabels(jsonResponse, description);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check image description: " + e.getMessage(), e);
        }
    }

    private String analyzeImageWithGoogleVision(String imageBase64) {
        String apiUrl = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

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

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
        return response.getBody();
    }

    private boolean analyzeLabels(String jsonResponse, String description) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode labels = root.path("responses").path(0).path("labelAnnotations");

        double threshold = 0.8;

        for (JsonNode label : labels) {
            String labelDescription = label.path("description").asText().toLowerCase();
            double score = label.path("score").asDouble();

            if (labelDescription.contains(description.toLowerCase()) && score >= threshold) {
                return true;
            }
        }
        return false;
    }
}
