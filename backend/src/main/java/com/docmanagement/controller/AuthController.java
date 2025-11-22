package com.docmanagement.controller;

import com.docmanagement.dto.AuthRequest;
import com.docmanagement.dto.AuthResponse;
import com.docmanagement.dto.RegisterRequest;
import com.docmanagement.model.User;
import com.docmanagement.service.JwtService;
import com.docmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    "USER"
            );
            
            String token = jwtService.generateToken(user);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .message("User registered successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .message("Login successful")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message("Invalid username or password")
                            .build());
        }
    }
}

