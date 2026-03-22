package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.service.MailService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ✅ REGISTER endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {

        Optional<User> existingUser =
                userRepository.findByEmail(newUser.getEmail());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email already exists");
        }

        // ✅ Default role
        if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
            newUser.setRole("STUDENT");
        }

        // ✅ Encrypt password before saving
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        userRepository.save(newUser);

        mailService.sendRegistrationMail(
                newUser.getEmail(),
                newUser.getName()
        );

        return ResponseEntity.ok("Registration successful");
    }

    // ✅ LOGIN endpoint (stateless)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {

        Optional<User> optionalUser =
                userRepository.findByEmail(loginRequest.getEmail());

        if (optionalUser.isPresent() &&
            passwordEncoder.matches(
                loginRequest.getPassword(),
                optionalUser.get().getPassword()
            )) {

            return ResponseEntity.ok(optionalUser.get());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
    }
}
