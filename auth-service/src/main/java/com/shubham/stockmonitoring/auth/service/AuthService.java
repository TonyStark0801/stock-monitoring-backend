package com.shubham.stockmonitoring.auth.service;

import com.shubham.stockmonitoring.auth.dto.request.LoginRequest;
import com.shubham.stockmonitoring.auth.dto.request.RegisterRequest;
import com.shubham.stockmonitoring.auth.dto.response.AuthResponse;
import com.shubham.stockmonitoring.auth.dto.response.RegisterResponse;
import com.shubham.stockmonitoring.auth.entity.User;
import com.shubham.stockmonitoring.auth.repository.UserRepository;
import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import com.shubham.stockmonitoring.commons.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;


    @Transactional
    public BaseResponse register(RegisterRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isPresent()) {
            throw new CustomException("USER_EXISTS", "Username already exists", HttpStatus.BAD_REQUEST);
        }

        String transactionId = otpService.generateAndSendOtp(request.getEmail());
        userRepository.save(
                User.builder()
                        .userId(UUID.randomUUID().toString())
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .enabled(false)
                        .build()
        );

        RegisterResponse registerResponse = RegisterResponse.builder()
                .transactionId(transactionId)
                .message("OTP sent to email for verification")
                .build();

        return BaseResponse.success(registerResponse);

    }

    public AuthResponse verifyEmailAndLogin(String email, String otp) {
        if (!otpService.validateOtp(email, otp)) {
            throw new CustomException("INVALID_OTP", "Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "User not found", HttpStatus.BAD_REQUEST));

        // Enable user after email verification
        user.setEnabled(true);
        userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "Bearer", user.getUserId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Get user details
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "User not found", HttpStatus.BAD_REQUEST));

        // Check if user is enabled
        if (!user.isEnabled()) {
            throw new CustomException("USER_NOT_ENABLED", "User account is not enabled. Please verify your email first.", HttpStatus.BAD_REQUEST);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.BAD_REQUEST);
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "Bearer", user.getUserId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public String validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException("INVALID_TOKEN", "Invalid token", org.springframework.http.HttpStatus.UNAUTHORIZED));

        if (!jwtService.isTokenValid(token, user)) {
            throw new CustomException("INVALID_TOKEN", "Token is invalid or expired", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        return user.getId().toString();
    }
}
