package com.worldconflict.api.controller;

import com.worldconflict.api.dto.LoginRequest;
import com.worldconflict.api.dto.RegisterRequest;
import com.worldconflict.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        Boolean rememberMe = request.get("rememberMe") != null ? (Boolean) request.get("rememberMe") : false;
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        return ResponseEntity.ok(authService.login(loginRequest, rememberMe));
    }
    
    @PostMapping("/login/remember")
    public ResponseEntity<Map<String, Object>> loginWithRemember(@RequestBody Map<String, String> request) {
        String rememberToken = request.get("rememberToken");
        return ResponseEntity.ok(authService.loginWithRememberToken(rememberToken));
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return ResponseEntity.ok(authService.forgotPassword(email));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");
        return ResponseEntity.ok(authService.resetPassword(resetToken, newPassword));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        
        var user = authService.getUserByToken(token.substring(7));
        if (user == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()
            )
        ));
    }
    
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");
        return ResponseEntity.ok(authService.updatePassword(username, newPassword));
    }
}
