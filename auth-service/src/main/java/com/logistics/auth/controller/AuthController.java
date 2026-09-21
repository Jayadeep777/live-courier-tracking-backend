package com.logistics.auth.controller;

import com.logistics.auth.dto.*;
import com.logistics.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/couriers/pending")
    public ResponseEntity<List<UserDto>> getPendingCouriers() {
        return ResponseEntity.ok(authService.getPendingCouriers());
    }

    @GetMapping("/couriers/approved")
    public ResponseEntity<List<UserDto>> getApprovedCouriers() {
        return ResponseEntity.ok(authService.getApprovedCouriers());
    }

    @GetMapping("/couriers")
    public ResponseEntity<List<UserDto>> getAllCouriers() {
        return ResponseEntity.ok(authService.getAllCouriers());
    }

    @PutMapping("/couriers/{id}/approve")
    public ResponseEntity<UserDto> approveCourier(@PathVariable Long id) {
        return ResponseEntity.ok(authService.approveCourier(id));
    }

    @PutMapping("/couriers/{id}/reject")
    public ResponseEntity<UserDto> rejectCourier(@PathVariable Long id) {
        return ResponseEntity.ok(authService.rejectCourier(id));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
