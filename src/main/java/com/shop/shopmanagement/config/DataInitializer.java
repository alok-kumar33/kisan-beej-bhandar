package com.shop.shopmanagement.config;

import com.shop.shopmanagement.entity.AppUser;
import com.shop.shopmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create Admin if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Default Password
            admin.setRole("ADMIN");
            admin.setFullName("Super Admin");
            userRepository.save(admin);
            System.out.println("✅ ADMIN USER CREATED: User=admin, Pass=admin123");
        }
    }
}