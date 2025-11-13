package com.shubham.stockmonitoring.auth.service;

import com.shubham.stockmonitoring.auth.dto.request.LoginRequest;
import com.shubham.stockmonitoring.auth.dto.request.RegisterRequest;
import com.shubham.stockmonitoring.auth.dto.request.ValidateOtpRequest;
import com.shubham.stockmonitoring.auth.dto.response.AuthResponse;
import com.shubham.stockmonitoring.auth.dto.response.GenerateOtpResponse;
import com.shubham.stockmonitoring.auth.entity.User;
import com.shubham.stockmonitoring.auth.repository.UserRepository;
import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import com.shubham.stockmonitoring.commons.exception.CustomException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.shubham.stockmonitoring.auth.Util.Constants.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final GoogleAuthService googleAuthService;


    @Transactional
    public BaseResponse register(RegisterRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isPresent()) {
            throw new CustomException("USER_EXISTS", "Username already exists", HttpStatus.BAD_REQUEST);
        }

        User newUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .build();
        userRepository.save(newUser);

        AuthResponse registerResponse = AuthResponse.builder()
                .name(newUser.getName())
                .email(newUser.getEmail())
                .token(jwtService.generateToken(newUser))
                .tokenType("Bearer")
                .isEnabled(newUser.isEnabled())
                .message(REGISTER_SUCCESS_MESSAGE)
                .build();

        return BaseResponse.success(registerResponse);

    }

    public BaseResponse verifyEmailAndLogin(ValidateOtpRequest request) {
        if (!otpService.validateOtp(request)) {
            throw new CustomException("INVALID_OTP", "Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "User not found", HttpStatus.BAD_REQUEST));

        user.setEnabled(true);
        userRepository.save(user);

        AuthResponse loginResponse =  AuthResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getUserId())
                .token(jwtService.generateToken(user))
                .tokenType("Bearer")
                .isEnabled(user.isEnabled())
                .message(OTP_VERIFIED_MESSAGE)
                .build();

        return BaseResponse.success(loginResponse);
    }

    public BaseResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "User not found", HttpStatus.BAD_REQUEST));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.BAD_REQUEST);
        }


        AuthResponse loginResponse =  AuthResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getUserId())
                .token(jwtService.generateToken(user))
                .tokenType("Bearer")
                .isEnabled(user.isEnabled())
                .message(LOGIN_SUCCESS_MESSAGE)
                .build();

        return BaseResponse.success(loginResponse);
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

    public BaseResponse generateOtp(@Valid @NotBlank(message = "Email is required") String email) {
        String transactionId = otpService.generateAndSendOtp(email);
        return BaseResponse.success(
                GenerateOtpResponse.builder()
                        .email(email)
                        .transactionId(transactionId)
                        .message(OTP_SENT_MESSAGE)
                        .build()
        );
    }
}
