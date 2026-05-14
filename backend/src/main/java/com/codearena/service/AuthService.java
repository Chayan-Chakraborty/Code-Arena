package com.codearena.service;

import com.codearena.dto.AuthDtos.*;
import com.codearena.entity.User;
import com.codearena.repository.UserRepository;
import com.codearena.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByUsername(req.username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(req.email)) {
            throw new RuntimeException("Email already exists");
        }
        User u = User.builder()
                .username(req.username)
                .email(req.email)
                .password(passwordEncoder.encode(req.password))
                .role("USER")
                .build();
        u = userRepository.save(u);
        String token = jwtUtil.generateToken(u.getUsername(), u.getId(), u.getRole());
        return new AuthResponse(token, u.getId(), u.getUsername(), u.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByUsername(req.username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password, u.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String role = u.getRole() == null ? "USER" : u.getRole();
        String token = jwtUtil.generateToken(u.getUsername(), u.getId(), role);
        return new AuthResponse(token, u.getId(), u.getUsername(), role);
    }
}
