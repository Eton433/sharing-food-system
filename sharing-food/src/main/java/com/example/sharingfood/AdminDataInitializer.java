package com.example.sharingfood;

import com.example.sharingfood.model.Admin;
import com.example.sharingfood.repository.AdminRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@Configuration
public class AdminDataInitializer {

    @Autowired
    private AdminRepository adminRepository;

    @Bean
    public CommandLineRunner initAdminData() {
        return args -> {
            ObjectMapper objectMapper = new ObjectMapper();

            // 讀取 JSON 檔案
            InputStream inputStream = new ClassPathResource("admin-data.json").getInputStream();
            List<Admin> admins = objectMapper.readValue(inputStream, new TypeReference<List<Admin>>() {});

            // 初始化管理員資料
            for (Admin admin : admins) {
                if (!adminRepository.existsByAdminId(admin.getAdminId())) {
                    adminRepository.save(admin);
                    System.out.println("Admin initialized: " + admin.getAdminId());
                } else {
                    System.out.println("Admin already exists: " + admin.getAdminId());
                }
            }
        };
    }
}
