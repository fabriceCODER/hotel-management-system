package com.fabish.hotel.service;

import com.fabish.hotel.model.User;
import com.fabish.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String[] ROLES = {"ADMIN", "STAFF", "USER"};
    private static final int PASSWORD_LENGTH = 12;
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";

    public User createUser(User user) {
        validateUserData(user);
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if none specified
        if (user.getRoles().isEmpty()) {
            user.getRoles().add("USER");
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        // Check if username or email is being changed and if it already exists
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsername(userDetails.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Update user details
        user.setUsername(userDetails.getUsername());
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        
        // Only update password if a new one is provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addRole(Long id, String role) {
        User user = getUserById(id);
        if (!isValidRole(role)) {
            throw new RuntimeException("Invalid role: " + role);
        }
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRole(Long id, String role) {
        User user = getUserById(id);
        if (!isValidRole(role)) {
            throw new RuntimeException("Invalid role: " + role);
        }
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    public void enableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void disableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    public String resetPassword(Long id) {
        User user = getUserById(id);
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    public void unlockAccount(Long id) {
        User user = getUserById(id);
        user.resetFailedLoginAttempts();
        userRepository.save(user);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.findByUsernameContainingOrFullNameContainingOrEmailContaining(
            query, query, query);
    }

    public void updateLastLogin(Long id) {
        User user = getUserById(id);
        user.setLastLogin(LocalDateTime.now());
        user.resetFailedLoginAttempts();
        userRepository.save(user);
    }

    public void handleFailedLogin(Long id) {
        User user = getUserById(id);
        user.incrementFailedLoginAttempts();
        userRepository.save(user);
    }

    private void validateUserData(User user) {
        if (user.getUsername() == null || user.getUsername().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters long");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
    }

    private boolean isValidRole(String role) {
        for (String validRole : ROLES) {
            if (validRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    private String generateRandomPassword() {
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one of each character type
        password.append(PASSWORD_CHARS.charAt(random.nextInt(26))); // Uppercase
        password.append(PASSWORD_CHARS.charAt(26 + random.nextInt(26))); // Lowercase
        password.append(PASSWORD_CHARS.charAt(52 + random.nextInt(10))); // Number
        password.append(PASSWORD_CHARS.charAt(62 + random.nextInt(10))); // Special char
        
        // Fill the rest randomly
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        
        // Shuffle the password
        String shuffledPassword = password.toString();
        List<Character> chars = shuffledPassword.chars()
                .mapToObj(e -> (char) e)
                .collect(Collectors.toList());
        java.util.Collections.shuffle(chars);
        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
} 