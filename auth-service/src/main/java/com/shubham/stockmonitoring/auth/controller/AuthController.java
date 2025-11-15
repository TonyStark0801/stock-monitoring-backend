package com.shubham.stockmonitoring.auth.controller;

import com.shubham.stockmonitoring.auth.dto.request.LoginRequest;
import com.shubham.stockmonitoring.auth.dto.request.OauthExchangeRequest;
import com.shubham.stockmonitoring.auth.dto.request.RegisterRequest;
import com.shubham.stockmonitoring.auth.dto.request.ValidateOtpRequest;
import com.shubham.stockmonitoring.auth.dto.response.OAuthCodeResponse;
import com.shubham.stockmonitoring.auth.service.AuthService;
import com.shubham.stockmonitoring.auth.service.OAuthCodeService;
import com.shubham.stockmonitoring.commons.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    private final OAuthCodeService oAuthCodeService;

    public AuthController(AuthService authService, OAuthCodeService oAuthCodeService) {
        this.authService = authService;
        this.oAuthCodeService = oAuthCodeService;
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


    @PostMapping("/oauth/exchange")
    public BaseResponse exchangeOAuthCode(@Valid @RequestBody OauthExchangeRequest request) {
        OAuthCodeResponse oauthData = oAuthCodeService.exchangeCode(request);
        return BaseResponse.success(oauthData);
    }

    @GetMapping("/health")
    public BaseResponse health() {
        return BaseResponse.success("Auth service is healthy");
    }
}
