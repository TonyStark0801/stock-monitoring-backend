package com.shubham.stockmonitoring.auth.handler;

import com.shubham.stockmonitoring.auth.entity.User;
import com.shubham.stockmonitoring.auth.repository.UserRepository;
import com.shubham.stockmonitoring.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.oauth.success-redirect}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            response.sendRedirect(successRedirectUrl + "?error=email_not_found");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth authentication"));

        String token = jwtService.generateToken(user);
        String targetUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam("token", token)
                .queryParam("email", user.getEmail())
                .queryParam("name", user.getName() != null ? user.getName() : "")
                .queryParam("userId", user.getUserId())
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
