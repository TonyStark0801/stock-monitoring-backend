package com.shubham.stockmonitoring.auth.service;

import com.shubham.stockmonitoring.auth.Util.AuthProvider;
import com.shubham.stockmonitoring.auth.entity.User;
import com.shubham.stockmonitoring.auth.repository.UserRepository;
import com.shubham.stockmonitoring.commons.exception.CustomException;
import com.shubham.stockmonitoring.commons.util.ObjectUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class GoogleAuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public GoogleAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        return processOAuth2User(registrationId, oAuth2User);
    }

    public OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String profileImage = oAuth2User.getAttribute("picture");

        if (ObjectUtil.isNullOrEmpty(email)) {
            throw new CustomException("GOOGLE_AUTH", "Email not found from OAuth2 provider", HttpStatus.BAD_REQUEST);
        }

        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {

            if (!optionalUser.get().getProvider().equals(authProvider)) {
                throw new CustomException("PROVIDER_MISMATCH",
                        "Looks like you're signed up with " + optionalUser.get().getProvider() +
                        " account. Please use your " + optionalUser.get().getProvider() +
                        " account to login.", HttpStatus.BAD_REQUEST);
            }

            user = optionalUser.get();
            user.setName(firstName + " " + lastName);
            user.setProfileImageUrl(profileImage);
            userRepository.save(user);
        }
        user = User.builder()
                .userId(java.util.UUID.randomUUID().toString())
                .name(firstName + " " + lastName)
                .email(email)
                .providerId(providerId)
                .provider(authProvider)
                .profileImageUrl(profileImage)
                .enabled(true)
                .build();
        userRepository.save(user);
        return oAuth2User;
    }

}
