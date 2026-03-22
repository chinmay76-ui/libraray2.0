package com.library.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.library.entity.User;
import com.library.repository.UserRepository;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===============================
    // SEND OTP FOR PASSWORD RESET
    // ===============================
    public void sendOtp(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Security: do not reveal if email exists
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        String otp = generateOtp();
        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        String body = """
                Hello %s,<br><br>
                You requested to reset your password.<br>
                Your OTP is:<br>
                <h2>%s</h2>
                This OTP is valid for <b>10 minutes</b>.<br><br>
                If you did not request this, please ignore this email.<br><br>
                Library Management System
                """.formatted(user.getName(), otp);

        mailService.sendEmail(email, "Password Reset OTP", body);
    }

    // ===============================
    // VERIFY OTP & RESET PASSWORD
    // ===============================
    public boolean resetPassword(String email, String otp, String newPassword) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        // Validate OTP and expiry
        if (user.getResetOtp() == null ||
            !user.getResetOtp().equals(otp) ||
            user.getOtpExpiry() == null ||
            user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear OTP after successful reset
        user.setResetOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
        return true;
    }

    // ===============================
    // OTP GENERATOR
    // ===============================
    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
