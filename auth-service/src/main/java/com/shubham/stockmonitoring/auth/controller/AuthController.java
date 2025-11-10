package com.shubham.stockmonitoring.auth.controller;

import com.shubham.stockmonitoring.auth.dto.request.LoginRequest;
import com.shubham.stockmonitoring.auth.dto.request.RegisterRequest;
import com.shubham.stockmonitoring.auth.dto.request.ValidateOtpRequest;
import com.shubham.stockmonitoring.auth.service.AuthService;
import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

   @PostMapping("/generateOtp")
   public BaseResponse generateOtp(@Valid @RequestHeader("X-CLIENT-EMAIL") String email) {
       return authService.generateOtp(email);
   }

    @PostMapping("/validateOtp")
    public BaseResponse validateOtp(@RequestBody ValidateOtpRequest request, @RequestHeader("X-CLIENT-EMAIL") String email) {
        request.setEmail(email);
        return authService.verifyEmailAndLogin(request);
    }
    
    @PostMapping("/login")
    public BaseResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/oauth2/google")
    public BaseResponse googleOAuth2Login(@RequestParam("token") String token) {
        return authService.googleOAuth2Login(token);
    }


    @GetMapping("/health")
    public BaseResponse health() {
        return BaseResponse.success("Auth service is healthy");
    }
}
