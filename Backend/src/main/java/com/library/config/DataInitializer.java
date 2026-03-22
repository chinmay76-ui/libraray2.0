package com.library.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.library.entity.User;
import com.library.repository.UserRepository;

import java.util.Optional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {

        Optional<User> adminOpt =
                userRepository.findByEmail("admin@gmail.com");

        // If admin does not exist, create it
        if (adminOpt.isEmpty()) {

            User newAdmin = new User();
            newAdmin.setName("System Admin");
            newAdmin.setEmail("admin@gmail.com");

            // Encrypt the password before saving
            String encryptedPassword =
                    new BCryptPasswordEncoder().encode("admin123");

            newAdmin.setPassword(encryptedPassword);
            newAdmin.setRole("ADMIN");

            userRepository.save(newAdmin);

            System.out.println(
                "✅ Default admin created: admin@gmail.com / admin123"
            );

        } else {
            System.out.println(
                "ℹ️ Admin already exists, skipping creation."
            );
        }
    }
}
