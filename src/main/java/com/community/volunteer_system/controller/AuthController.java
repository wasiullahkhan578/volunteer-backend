package com.community.volunteer_system.controller;

import com.community.volunteer_system.dto.*;
import com.community.volunteer_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Milestone 3: Role-Based Registration
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String response = authService.register(request);
            // Returning as a JSON object to match React frontend expectations
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(java.util.Map.of("message", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /**
     * Role-Aware Login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Returns LoginResponse containing token, role, and firstName
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 401 Unauthorized for bad credentials or unapproved organizers
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /**
     * Email Verification with Frontend Redirect
     */
    @GetMapping("/verify")
    public ResponseEntity<Void> verifyUser(@RequestParam("token") String token) {
        try {
            authService.verifyUser(token);
            // Redirects to login page with a success flag for the UI
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/login?verified=true"))
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/register?error=invalid_token"))
                    .build();
        }
    }

    /**
     * Password Recovery Logic
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok(java.util.Map.of("message", "Reset link sent to your email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.updatePassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(java.util.Map.of("message", "Password reset successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}