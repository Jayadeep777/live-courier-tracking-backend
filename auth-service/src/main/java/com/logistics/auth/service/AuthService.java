package com.logistics.auth.service;

import com.logistics.auth.dto.*;
import com.logistics.auth.model.User;
import com.logistics.auth.repository.UserRepository;
import com.logistics.auth.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address already registered: " + request.getEmail());
        }

        com.logistics.auth.model.Role requestedRole = request.getRole() != null ? request.getRole() : com.logistics.auth.model.Role.USER;

        if (requestedRole == com.logistics.auth.model.Role.ADMIN) {
            throw new RuntimeException("Admin registration is forbidden. Single admin account is pre-configured.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(requestedRole);

        if (requestedRole == com.logistics.auth.model.Role.COURIER) {
            user.setStatus("PENDING");
            User savedUser = userRepository.save(user);
            // Return null token for pending courier so auto-login is prevented
            return new AuthResponse(null, mapToDto(savedUser));
        } else {
            user.setStatus("APPROVED");
            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(savedUser);
            return new AuthResponse(token, mapToDto(savedUser));
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getRole() == com.logistics.auth.model.Role.COURIER) {
            if ("PENDING".equalsIgnoreCase(user.getStatus())) {
                throw new RuntimeException("Your Delivery Partner registration request is pending Admin review and approval.");
            }
            if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
                throw new RuntimeException("Your Delivery Partner registration request was rejected by Admin.");
            }
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, mapToDto(user));
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getPendingCouriers() {
        return userRepository.findByRoleAndStatus(com.logistics.auth.model.Role.COURIER, "PENDING").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getApprovedCouriers() {
        return userRepository.findByRoleAndStatus(com.logistics.auth.model.Role.COURIER, "APPROVED").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getAllCouriers() {
        return userRepository.findByRole(com.logistics.auth.model.Role.COURIER).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto approveCourier(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery Partner not found with id: " + id));
        user.setStatus("APPROVED");
        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    public UserDto rejectCourier(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery Partner not found with id: " + id));
        user.setStatus("REJECTED");
        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus(), user.getCreatedAt());
    }
}
