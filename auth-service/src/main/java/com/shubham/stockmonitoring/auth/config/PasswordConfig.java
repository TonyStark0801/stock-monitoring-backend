package com.shubham.stockmonitoring.auth.config;

import com.shubham.stockmonitoring.auth.repository.UserRepository;
import com.shubham.stockmonitoring.auth.service.GoogleAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

@Configuration("authPasswordConfig")
public class PasswordConfig {

    private final UserRepository userRepository;

    public PasswordConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2UserService<org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest, org.springframework.security.oauth2.core.user.OAuth2User> oAuth2UserService() {
        return new GoogleAuthService(userRepository);
    }
}

