package com.worldconflict.api.config;

import com.worldconflict.api.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    
    private final AuthService authService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        Map<String, Object> result = authService.processGoogleLogin(googleId, email, name != null ? name : email);
        
        String token = (String) result.get("token");
        Map<String, Object> user = (Map<String, Object>) result.get("user");
        
        String userJson = user.get("id") + "," + user.get("username") + "," + user.get("displayName");
        
        response.sendRedirect("http://localhost:5176?login=google&token=" + token + "&user=" + userJson);
    }
}
