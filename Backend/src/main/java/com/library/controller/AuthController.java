package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.library.entity.User;
import com.library.service.UserService;
import com.library.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    // ==========================
    // ✅ User Login (UNCHANGED)
    // ==========================
    @PostMapping("/login")
    public User login(@RequestBody User loginRequest) {
        User user = userService.validateLogin(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (user != null) {
            System.out.println("✅ Login successful for: " + user.getEmail());
            return user;
        }

        System.out.println("❌ Invalid login attempt for email: " + loginRequest.getEmail());
        return null;
    }

    // ==========================
    // ✅ User Registration (UNCHANGED)
    // ==========================
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // ==========================
    // 🔐 Forgot Password – Send OTP
    // ==========================
   

    // ==========================
    // 🔐 Reset Password using OTP
    
}
