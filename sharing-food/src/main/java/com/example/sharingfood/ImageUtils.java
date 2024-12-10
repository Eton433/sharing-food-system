package com.example.sharingfood;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;

public class ImageUtils {

    // 圖片處理：縮放圖片，轉為 JPG 格式並轉換為 Base64 編碼
    public static String processImage(MultipartFile imageFile) throws IOException {
        // 讀取圖片
        BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());

        // 設定圖片的最大寬度和高度
        int maxWidth = 600;  // 最大寬度
        int maxHeight = 600; // 最大高度

        // 縮放圖片
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double aspectRatio = (double) width / height;

        // 根據寬度或高度來計算新的尺寸
        if (width > height) {
            width = maxWidth;
            height = (int) (width / aspectRatio);
        } else {
            height = maxHeight;
            width = (int) (height * aspectRatio);
        }

        // 創建縮放後的圖片
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 複製縮放後的圖片到新的圖片對象
        Graphics2D graphics2D = outputImage.createGraphics();
        graphics2D.drawImage(scaledImage, 0, 0, null);
        graphics2D.dispose();

        // 將處理後的圖片轉為 Base64 編碼
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(outputImage, "jpg", byteArrayOutputStream);  // 轉為 JPG 格式
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);  // 返回 Base64 編碼
    }
}
