package com.fabish.hotel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret;
    private long jwtExpirationInMs;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Set up test values for JWT properties
        jwtSecret = "testSecretKeyWithAtLeast32CharactersForHS256Algorithm";
        jwtExpirationInMs = 3600000; // 1 hour
        
        // Use reflection to set private fields
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", jwtExpirationInMs);
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        // Arrange
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        when(userPrincipal.getId()).thenReturn(1L);
        when(userPrincipal.getUsername()).thenReturn("testuser");
        when(userPrincipal.getFullName()).thenReturn("Test User");
        when(userPrincipal.getEmail()).thenReturn("test@example.com");
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        // Act
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        
        // Verify token contents
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals("testuser", claims.getSubject());
        assertEquals(1L, claims.get("userId"));
        assertEquals("Test User", claims.get("fullName"));
        assertEquals("test@example.com", claims.get("email"));
        
        // Verify expiration
        Date now = new Date();
        Date expiration = claims.getExpiration();
        assertTrue(expiration.after(now));
        long diff = expiration.getTime() - now.getTime();
        // Allow for a small difference due to test execution time
        assertTrue(diff <= jwtExpirationInMs && diff > jwtExpirationInMs - 5000);
    }

    @Test
    void getUsernameFromJWT_ShouldReturnCorrectUsername() {
        // Arrange
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        when(userPrincipal.getId()).thenReturn(1L);
        when(userPrincipal.getUsername()).thenReturn("testuser");
        when(userPrincipal.getFullName()).thenReturn("Test User");
        when(userPrincipal.getEmail()).thenReturn("test@example.com");
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Act
        String username = jwtTokenProvider.getUsernameFromJWT(token);
        
        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        when(userPrincipal.getId()).thenReturn(1L);
        when(userPrincipal.getUsername()).thenReturn("testuser");
        when(userPrincipal.getFullName()).thenReturn("Test User");
        when(userPrincipal.getEmail()).thenReturn("test@example.com");
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalidToken";
        
        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        // Create a token that's already expired
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() - 1000); // 1 second in the past
        
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
        
        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);
        
        // Assert
        assertFalse(isValid);
    }
}