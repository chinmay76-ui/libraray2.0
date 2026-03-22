package com.library.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    // ==================================
    // SEND OTP
    // ==================================
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Map<String, String> request) {

        String email = request.get("email");

        passwordResetService.sendOtp(email);

        // Do not reveal if email exists (security)
        return "If the email exists, an OTP has been sent.";
    }

    // ==================================
    // VERIFY OTP
    // ==================================
    @PostMapping("/verify-otp")
    public boolean verifyOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        if (user.getResetOtp() == null ||
            !user.getResetOtp().equals(otp) ||
            user.getOtpExpiry() == null ||
            user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    // ==================================
    // RESET PASSWORD
    // ==================================
    @PostMapping("/reset-password")
    public boolean resetPassword(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        return passwordResetService.resetPassword(email, otp, newPassword);
    }
}