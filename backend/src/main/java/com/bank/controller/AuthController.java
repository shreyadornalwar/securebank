package com.bank.controller;

import com.bank.config.JwtUtil;
import com.bank.model.LoginRequest;
import com.bank.model.User;
import com.bank.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.authenticate(request.getEmail(), request.getPassword(), request.getRole())
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    Map<String, Object> safeUser = new HashMap<>();
                    safeUser.put("id", user.getId());
                    safeUser.put("email", user.getEmail());
                    safeUser.put("role", user.getRole());
                    safeUser.put("name", user.getFirstName() + " " + user.getLastName());
                    safeUser.put("accountId", user.getAccountId());
                    return ResponseEntity.ok(Map.of("token", token, "user", safeUser));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("message", "Invalid credentials")));
    }
}
