package com.shubham.stockmonitoring.auth.controller;

import com.shubham.stockmonitoring.auth.dto.response.AuthResponse;
import com.shubham.stockmonitoring.auth.dto.request.LoginRequest;
import com.shubham.stockmonitoring.auth.dto.request.RegisterRequest;
import com.shubham.stockmonitoring.auth.service.AuthService;
import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public BaseResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    
//    @PostMapping("/login")
//    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
//        AuthResponse response = authService.login(request);
//        return ResponseEntity.ok(BaseResponse.success("Login successful", response));
//    }
//
//    @PostMapping("/verify-email")
//    public ResponseEntity<BaseResponse<AuthResponse>> verifyEmail(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        String otp = request.get("otp");
//        AuthResponse response = authService.verifyEmailAndLogin(email, otp);
//        return ResponseEntity.ok(BaseResponse.success("Email verified successfully", response));
//    }
//
//    @PostMapping("/validate")
//    public ResponseEntity<BaseResponse<String>> validateToken(@RequestHeader("Authorization") String token) {
//        String userId = authService.validateToken(token);
//        return ResponseEntity.ok(BaseResponse.success("Token is valid", userId));
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<BaseResponse<String>> health() {
//        return ResponseEntity.ok(BaseResponse.success("Auth service is healthy", "OK"));
//    }
}
