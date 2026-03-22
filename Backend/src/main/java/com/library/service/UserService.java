package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.library.entity.User;
import com.library.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String PERMANENT_ADMIN_EMAIL = "admin@gmail.com";

    @PostConstruct
    public void createDefaultAdmin() {

        Optional<User> adminOpt =
                userRepository.findByEmail(PERMANENT_ADMIN_EMAIL);

        if (adminOpt.isEmpty()) {

            User admin = new User();
            admin.setEmail(PERMANENT_ADMIN_EMAIL);
            admin.setName("Admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");

            userRepository.save(admin);

            System.out.println(
                "✅ Default admin created: admin@gmail.com / admin123"
            );
        }
    }

    // ✅ Register new user (Student or Librarian)
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ✅ Login validation using BCrypt
    public User validateLogin(String email, String rawPassword) {

        Optional<User> userOpt =
                userRepository.findByEmail(email);

        if (userOpt.isPresent() &&
            passwordEncoder.matches(
                rawPassword,
                userOpt.get().getPassword()
            )) {
            return userOpt.get();
        }

        return null;
    }

    // ✅ Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ✅ Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ✅ Update role
    public boolean updateUserRole(Long id, String newRole) {

        User user = getUserById(id);

        if (user != null) {
            user.setRole(newRole);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
