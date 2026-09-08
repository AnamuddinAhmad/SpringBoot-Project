package com.auth.Auth.security;

import com.auth.Auth.entity.RefreshToken;
import com.auth.Auth.entity.Users;
import com.auth.Auth.exception.ValidationError;
import com.auth.Auth.repo.RefreshTokenRepo;
import com.auth.Auth.repo.UserRepo;
import com.auth.Auth.utils.Provider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepo userRepo;
    private final JWTServices jwtServices;
    private final CookieService cookieService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Value("${app.auth.frontend.success-redirect}")
    private String successRedirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        String registrationId = "Unknow";
        if (authentication instanceof OAuth2AuthenticationToken token){
            registrationId = token.getAuthorizedClientRegistrationId() ;
        }

        Users user = null;
        switch (registrationId){
            case "google" -> {

                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String emailId = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                logger.info("Google picture URL length: {}", picture.length());
                logger.info("Google picture URL: {}", picture);


                Users buildedUser = Users.builder()
                        .providerId(googleId)
                        .name(name)
                        .email(emailId)
                        .image(picture)
                        .provider(Provider.GOOGLE)
                        .enabled(true)
                        .build();
                user = userRepo.findByEmail(emailId).orElseGet(()->userRepo.save(buildedUser));
            }
            case "github" -> {
                String githubId = oAuth2User.getAttributes().getOrDefault("id","").toString();
                String emailId = oAuth2User.getAttributes().getOrDefault("email","").toString();
                if (emailId == null || emailId.isBlank()) {
                    throw new ValidationError(
                            "GitHub account does not have a public email address.",
                            HttpStatus.BAD_REQUEST
                    );
                }
                String name = oAuth2User.getAttributes().getOrDefault("name","").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("avatar_url","").toString();
                Users builderUser = Users.builder()
                        .providerId(githubId)
                        .name(name)
                        .email(emailId)
                        .image(picture)
                        .provider(Provider.GITHUB)
                        .enabled(true)
                        .build();
                logger.info("Built GitHub User: {}", builderUser);
                user = userRepo.findByEmail(emailId).orElseGet(()-> userRepo.save(builderUser));
            }
            default -> {
                throw new ValidationError("Invalid Case ", HttpStatus.BAD_REQUEST);
            }
        }

        String jti = UUID.randomUUID().toString();
        RefreshToken buildedRefreshToken = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now()
                        .plusSeconds(jwtServices.getRefreshTTLSecond()))
                .revoked(false)
                .build();
        refreshTokenRepo.save(buildedRefreshToken);
        String accessToken = jwtServices.genrateAccessToken(user);
        String refreshToken = jwtServices.genrateRefreshToken(user,buildedRefreshToken.getJti());
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtServices.getRefreshTTLSecond());
        response.sendRedirect(successRedirect);
    }
}
