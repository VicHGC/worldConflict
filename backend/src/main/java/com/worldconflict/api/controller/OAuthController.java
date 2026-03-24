package com.worldconflict.api.controller;

import com.worldconflict.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {
    
    private final AuthService authService;
    
    @GetMapping("/google")
    public ResponseEntity<Map<String, Object>> googleAuth() {
        return ResponseEntity.ok(Map.of(
            "message", "Redirect to /oauth2/authorization/google for Google OAuth2 login",
            "redirectUrl", "/oauth2/authorization/google"
        ));
    }
    
    @GetMapping("/google/callback")
    public ResponseEntity<Map<String, Object>> googleCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Authentication failed"));
        }
        
        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        if (email == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Email not provided by Google"));
        }
        
        return ResponseEntity.ok(authService.processGoogleLogin(googleId, email, name != null ? name : email));
    }
}
