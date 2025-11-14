package com.shubham.stockmonitoring.auth.handler;

import com.shubham.stockmonitoring.auth.entity.User;
import com.shubham.stockmonitoring.auth.repository.UserRepository;
import com.shubham.stockmonitoring.auth.service.JwtService;
import com.shubham.stockmonitoring.auth.service.OAuthCodeService;
import com.shubham.stockmonitoring.commons.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String SESSION_ATTRIBUTE_CODE_CHALLENGE = "oauth_code_challenge";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OAuthCodeService oAuthCodeService;

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
            log.warn("OAuth authentication succeeded but email is missing");
            response.sendRedirect(successRedirectUrl + "?error=email_not_found");
            return;
        }

        log.info("OAuth authentication successful for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth authentication"));

        String token = jwtService.generateToken(user);
        String codeChallenge = (String) request.getSession().getAttribute(SESSION_ATTRIBUTE_CODE_CHALLENGE);

        if(ObjectUtil.isNullOrEmpty(codeChallenge)) {
            log.error("PKCE code_challenge missing in session for user: {}", email);
            response.sendRedirect(successRedirectUrl + "?error=pkce_required");
            return;
        }

        String code = oAuthCodeService.generateCode(
                token,
                user.getEmail(),
                user.getName() != null ? user.getName() : "",
                user.getUserId(),
                user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "",
                codeChallenge
        );

        request.getSession().removeAttribute(SESSION_ATTRIBUTE_CODE_CHALLENGE);
        log.info("OAuth code generated successfully. Redirecting to frontend. User: {}", email);
        
        String targetUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam("code", code)
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
