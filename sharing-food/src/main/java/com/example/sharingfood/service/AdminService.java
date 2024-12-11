package com.example.sharingfood.service;

import com.example.sharingfood.model.Admin;
import com.example.sharingfood.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    public Admin findByAdminId(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    public Admin findByUsername(String username) {
        return adminRepository.findByAdminId(username);
    }

    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }
}
