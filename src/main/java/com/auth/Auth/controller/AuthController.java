package com.auth.Auth.controller;

import com.auth.Auth.dto.LoginRequest;
import com.auth.Auth.dto.RefreshTokenRequest;
import com.auth.Auth.dto.TokenResponse;
import com.auth.Auth.dto.UserDTO;
import com.auth.Auth.entity.RefreshToken;
import com.auth.Auth.entity.Users;
import com.auth.Auth.exception.ValidationError;
import com.auth.Auth.repo.RefreshTokenRepo;
import com.auth.Auth.repo.UserRepo;
import com.auth.Auth.security.CookieService;
import com.auth.Auth.security.JWTServices;
import com.auth.Auth.services.AuthServices;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthServices authServices;
    private final UserRepo userRepo;
    private final JWTServices jwtServices;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepo refreshTokenRepo;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        authenticate(loginRequest);
        Users user = userRepo.findByEmail(loginRequest.email()).orElseThrow(()->
                new UsernameNotFoundException("User not found"));

        if (!user.isEnabled()){
           throw ValidationError.forbidden("Account is disabled.");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenId = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtServices.getRefreshTTLSecond()))
                .revoked(false)
                .build();

        refreshTokenRepo.save(refreshTokenId);
        //genrate token.
        String accessToken = jwtServices.genrateAccessToken(user);
        String refreshToken = jwtServices.genrateRefreshToken(user, refreshTokenId.getJti());
        //USerCookie Service
        cookieService.attachRefreshCookie(response, refreshToken, (int)jwtServices.getRefreshTTLSecond());
        cookieService.addNoHeaders(response);
        TokenResponse tokenResponse = TokenResponse.of(accessToken,refreshToken,
                jwtServices.getAccessTTLSecond(),modelMapper.map(user,UserDTO.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private void authenticate(LoginRequest loginRequest) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

        } catch (BadCredentialsException | UsernameNotFoundException e) {

            throw ValidationError.unauthorized(
                    "Invalid email or password."
            );

        } catch (InternalAuthenticationServiceException e) {

            throw ValidationError.unauthorized(
                    "Invalid email or password."
            );

        } catch (DisabledException e) {

            throw ValidationError.forbidden(
                    "Account is disabled."
            );

        } catch (LockedException e) {

            throw ValidationError.forbidden(
                    "Account is locked."
            );

        } catch (AccountExpiredException e) {

            throw ValidationError.forbidden(
                    "Account has expired."
            );

        } catch (CredentialsExpiredException e) {

            throw ValidationError.unauthorized(
                    "Credentials have expired."
            );

        } catch (AuthenticationException e) {

            throw ValidationError.unauthorized(
                    "Authentication failed."
            );
        }
    }


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO){
       return ResponseEntity.status(HttpStatus.CREATED).body(authServices.registerUser(userDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
                                                      HttpServletResponse response,
                                                      HttpServletRequest request){
        String refreshToken = readRefreshTokenReq(body,request).orElseThrow(()->
                new ValidationError( "Refresh token not found.",HttpStatus.UNAUTHORIZED));

        if(!jwtServices.isRefreshToken(refreshToken)){
            throw ValidationError.badRequest( "Invalid refresh token.");
        }

        String jti = jwtServices.getJti(refreshToken);
        UUID userId = jwtServices.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepo.findByJti(jti).orElseThrow(() ->
                new ValidationError("Invalid JTI, RefreshToekn not reconozied.", HttpStatus.BAD_REQUEST));
        if (storedRefreshToken.isRevoked()){
            throw ValidationError.unauthorized("Refresh token is revoked.");
        }
        if (storedRefreshToken.getExpiredAt().isBefore(Instant.now())){
            throw ValidationError.unauthorized("Refresh token is expired.");
        }
        if (!storedRefreshToken.getUser().getId().equals(userId)){
            throw ValidationError.unauthorized(
                    "Invalid refresh token."
            );
        }
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepo.save(storedRefreshToken);

        var user = storedRefreshToken.getUser();
        var newRefreshToken = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtServices.getRefreshTTLSecond()))
                .revoked(false)
                .build();
        refreshTokenRepo.save(newRefreshToken);
        String accessToken = jwtServices.genrateAccessToken(user);
        String newGeneratedrefreshToken = jwtServices.genrateRefreshToken(user,newRefreshToken.getJti());

        cookieService.attachRefreshCookie(response,newGeneratedrefreshToken,(int) jwtServices.getRefreshTTLSecond());
        cookieService.addNoHeaders(response);
        return ResponseEntity.ok(TokenResponse.of(accessToken,newGeneratedrefreshToken,jwtServices.getAccessTTLSecond(),modelMapper.map(user,UserDTO.class)));
    }

    //This method will read refresh token from the request body
    private Optional<String> readRefreshTokenReq(RefreshTokenRequest body, HttpServletRequest request) {
        if (request.getCookies() != null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies()).filter(
                    (c)-> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue).filter(v -> !v.isBlank())
                    .findFirst();

            if (fromCookie.isPresent()){
                return fromCookie;
            }
        }
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()){
            return  Optional.of(body.refreshToken());
        }

        //Custom refresh header.
        String refreshHeader = request.getHeader("X-Refresh-Toke");
        if (refreshHeader != null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        //Authorization = Bearer<Token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true,0, "Bearer ",0,7)){
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()){
                try {
                    if (jwtServices.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    throw ValidationError.unauthorized(
                            "Invalid refresh token."
                    );
                }
            }
        }
        return Optional.empty();
    }
}
