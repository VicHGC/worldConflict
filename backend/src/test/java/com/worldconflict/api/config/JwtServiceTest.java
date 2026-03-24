package com.worldconflict.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    
    private JwtService jwtService;
    
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "WorldConflictSecretKeyForJWTGeneration2024MustBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 86400000L);
    }
    
    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(1L, "testuser");
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }
    
    @Test
    void testGenerateTokenForGoogle() {
        String token = jwtService.generateTokenForGoogle(1L, "test@test.com", "Test User");
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }
    
    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken(1L, "testuser");
        
        assertTrue(jwtService.isTokenValid(token));
    }
    
    @Test
    void testIsTokenValid_InvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }
    
    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(1L, "testuser");
        
        String username = jwtService.extractUsername(token);
        
        assertEquals("testuser", username);
    }
    
    @Test
    void testExtractUserId() {
        String token = jwtService.generateToken(1L, "testuser");
        
        Long userId = jwtService.extractUserId(token);
        
        assertEquals(1L, userId);
    }
    
    @Test
    void testExtractAllClaims() {
        String token = jwtService.generateToken(1L, "testuser");
        
        var claims = jwtService.extractAllClaims(token);
        
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
    }
}