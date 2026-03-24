package com.worldconflict.api.service;

import com.worldconflict.api.config.JwtService;
import com.worldconflict.api.dto.LoginRequest;
import com.worldconflict.api.dto.RegisterRequest;
import com.worldconflict.api.entity.User;
import com.worldconflict.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtService, passwordEncoder);
    }
    
    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setDisplayName("New User");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(1L, "newuser")).thenReturn("jwt-token");
        
        Map<String, Object> result = authService.register(request);
        
        assertTrue((Boolean) result.get("success"));
        assertEquals("jwt-token", result.get("token"));
        assertNotNull(result.get("user"));
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testRegister_UsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@test.com");
        request.setPassword("password123");
        
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        Map<String, Object> result = authService.register(request);
        
        assertFalse((Boolean) result.get("success"));
        assertEquals("Username already exists", result.get("message"));
    }
    
    @Test
    void testRegister_EmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
        
        Map<String, Object> result = authService.register(request);
        
        assertFalse((Boolean) result.get("success"));
        assertEquals("Email already exists", result.get("message"));
    }
    
    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        user.setDisplayName("Test User");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(1L, "testuser")).thenReturn("jwt-token");
        
        Map<String, Object> result = authService.login(request);
        
        assertTrue((Boolean) result.get("success"));
        assertEquals("jwt-token", result.get("token"));
    }
    
    @Test
    void testLogin_InvalidUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");
        
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        Map<String, Object> result = authService.login(request);
        
        assertFalse((Boolean) result.get("success"));
        assertEquals("Invalid username or password", result.get("message"));
    }
    
    @Test
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);
        
        Map<String, Object> result = authService.login(request);
        
        assertFalse((Boolean) result.get("success"));
        assertEquals("Invalid username or password", result.get("message"));
    }
    
    @Test
    void testProcessGoogleLogin_NewUser() {
        String googleId = "google123";
        String email = "google@test.com";
        String name = "Google User";
        
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateTokenForGoogle(1L, email, name)).thenReturn("google-jwt-token");
        
        Map<String, Object> result = authService.processGoogleLogin(googleId, email, name);
        
        assertTrue((Boolean) result.get("success"));
        assertEquals("google-jwt-token", result.get("token"));
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testProcessGoogleLogin_ExistingGoogleUser() {
        String googleId = "google123";
        String email = "google@test.com";
        String name = "Google User";
        
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("googleuser");
        existingUser.setEmail(email);
        
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateTokenForGoogle(2L, email, name)).thenReturn("google-jwt-token");
        
        Map<String, Object> result = authService.processGoogleLogin(googleId, email, name);
        
        assertTrue((Boolean) result.get("success"));
        assertEquals("google-jwt-token", result.get("token"));
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        User result = authService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}