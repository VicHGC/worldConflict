package com.worldconflict.api.service;

import com.worldconflict.api.config.JwtService;
import com.worldconflict.api.dto.LoginRequest;
import com.worldconflict.api.dto.RegisterRequest;
import com.worldconflict.api.entity.User;
import com.worldconflict.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    public Map<String, Object> login(LoginRequest request) {
        return login(request, false);
    }
    
    public Map<String, Object> login(LoginRequest request, boolean rememberMe) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Map.of("success", false, "message", "Invalid username or password");
        }
        
        String token;
        if (rememberMe) {
            token = jwtService.generateTokenExtended(user.getId(), user.getUsername());
            String rememberToken = UUID.randomUUID().toString();
            user.setRememberToken(rememberToken);
            userRepository.save(user);
        } else {
            token = jwtService.generateToken(user.getId(), user.getUsername());
        }
        
        return Map.of(
            "success", true,
            "token", token,
            "rememberToken", rememberMe ? user.getRememberToken() : "",
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()
            )
        );
    }
    
    public Map<String, Object> loginWithRememberToken(String rememberToken) {
        if (rememberToken == null || rememberToken.isEmpty()) {
            return Map.of("success", false, "message", "Invalid token");
        }
        
        User user = userRepository.findByRememberToken(rememberToken).orElse(null);
        if (user == null) {
            return Map.of("success", false, "message", "Invalid token");
        }
        
        String token = jwtService.generateTokenExtended(user.getId(), user.getUsername());
        
        return Map.of(
            "success", true,
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()
            )
        );
    }
    
    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return Map.of("success", false, "message", "Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            return Map.of("success", false, "message", "Email already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
        
        user = userRepository.save(user);
        
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        
        return Map.of(
            "success", true,
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName()
            )
        );
    }
    
    public Map<String, Object> processGoogleLogin(String googleId, String email, String name) {
        User user = userRepository.findByGoogleId(googleId).orElse(null);
        
        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                user = new User();
                user.setUsername(email.split("@")[0]);
                user.setEmail(email);
                user.setGoogleId(googleId);
                user.setDisplayName(name);
                user.setPassword(passwordEncoder.encode("GOOGLE_AUTH"));
                user = userRepository.save(user);
                log.info("New user created via Google: {}", email);
            } else {
                user.setGoogleId(googleId);
                user = userRepository.save(user);
                log.info("Google ID linked to existing user: {}", email);
            }
        }
        
        String token = jwtService.generateTokenForGoogle(user.getId(), email, name);
        
        return Map.of(
            "success", true,
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                "isGoogle", true
            )
        );
    }
    
    public Map<String, Object> forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return Map.of("success", false, "message", "Email not found");
        }
        
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpires(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        
        log.info("Password reset token generated for user: {} - Token: {}", email, resetToken);
        
        return Map.of(
            "success", true,
            "message", "Password reset link sent to your email",
            "resetToken", resetToken
        );
    }
    
    public Map<String, Object> resetPassword(String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isEmpty()) {
            return Map.of("success", false, "message", "Invalid reset token");
        }
        
        User user = userRepository.findByResetToken(resetToken).orElse(null);
        
        if (user == null) {
            return Map.of("success", false, "message", "Invalid reset token");
        }
        
        if (user.getResetTokenExpires() == null || user.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            return Map.of("success", false, "message", "Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpires(null);
        userRepository.save(user);
        
        log.info("Password reset successful for user: {}", user.getUsername());
        
        return Map.of("success", true, "message", "Password reset successful");
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public void createTestUser() {
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@worldconflict.com");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setDisplayName("Test User");
            userRepository.save(testUser);
            log.info("Test user created: testuser / test123");
        }
    }

    public Map<String, Object> updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Map.of("success", false, "message", "User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user: {}", username);
        return Map.of("success", true, "message", "Password updated");
    }
    
    public void logout(String token) {
        log.info("User logged out");
    }
    
    public User getUserByToken(String token) {
        try {
            if (jwtService.isTokenValid(token)) {
                final Long userId = jwtService.extractUserId(token);
                return userRepository.findById(userId).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Invalid token: {}", e.getMessage());
        }
        return null;
    }
}