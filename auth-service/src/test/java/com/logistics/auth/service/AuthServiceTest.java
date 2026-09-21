package com.logistics.auth.service;

import com.logistics.auth.dto.AuthResponse;
import com.logistics.auth.dto.LoginRequest;
import com.logistics.auth.dto.RegisterRequest;
import com.logistics.auth.model.Role;
import com.logistics.auth.model.User;
import com.logistics.auth.repository.UserRepository;
import com.logistics.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Test User", "test@logistics.com", "hashed_pass", Role.USER, LocalDateTime.now());
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("Test User", "test@logistics.com", "password123", Role.USER);
        
        when(userRepository.existsByEmail("test@logistics.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock_jwt_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("test@logistics.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("test@logistics.com", "password123");

        when(userRepository.findByEmail("test@logistics.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "hashed_pass")).thenReturn(true);
        when(jwtUtil.generateToken(sampleUser)).thenReturn("mock_jwt_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        verify(jwtUtil, times(1)).generateToken(sampleUser);
    }

    @Test
    void testAdminRegistrationBlocked() {
        RegisterRequest request = new RegisterRequest("Admin", "admin@test.com", "pass", Role.ADMIN);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertTrue(exception.getMessage().contains("Admin registration is forbidden"));
    }

    @Test
    void testCourierRegistrationPendingApproval() {
        RegisterRequest request = new RegisterRequest("Delivery Partner", "courier@test.com", "pass", Role.COURIER);
        User pendingCourier = new User(2L, "Delivery Partner", "courier@test.com", "hashed_pass", Role.COURIER, "PENDING", LocalDateTime.now());

        when(userRepository.existsByEmail("courier@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenReturn(pendingCourier);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNull(response.getToken()); // Token must be null until admin approves
        assertEquals("PENDING", response.getUser().getStatus());
    }

    @Test
    void testPendingCourierLoginBlocked() {
        LoginRequest request = new LoginRequest("courier@test.com", "pass");
        User pendingCourier = new User(2L, "Delivery Partner", "courier@test.com", "hashed_pass", Role.COURIER, "PENDING", LocalDateTime.now());

        when(userRepository.findByEmail("courier@test.com")).thenReturn(Optional.of(pendingCourier));
        when(passwordEncoder.matches("pass", "hashed_pass")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertTrue(exception.getMessage().contains("pending Admin review and approval"));
    }
}
