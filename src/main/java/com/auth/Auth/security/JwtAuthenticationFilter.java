package com.auth.Auth.security;

import com.auth.Auth.exception.ValidationError;
import com.auth.Auth.repo.UserRepo;
import com.auth.Auth.utils.UserHelper;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTServices jwtServices;
    private final UserRepo userRepo;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        logger.info("Authorization header : {}",header);
        if (header != null && header.startsWith("Bearer")){
            try {
                String token = header.substring(7);
                if (!jwtServices.isAccessToken(token)){
                    throw new ValidationError(
                            "Invalid access token.",
                            HttpStatus.UNAUTHORIZED
                    );
                }

                Jws<Claims> parse = jwtServices.parse(token);
                Claims payload = parse.getPayload();
                String userId = payload.getSubject();
                UUID id = UserHelper.parseUUID(userId);
                userRepo.findById(id).ifPresent(user -> {
                    if (!user.isEnabled()) {
                        throw new ValidationError("Accoutn is not Enabled.", HttpStatus.UNAUTHORIZED);
                    }
                        List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles()
                                .stream().map(roles -> new SimpleGrantedAuthority(roles.getName())).collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                authorities
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        if (SecurityContextHolder.getContext().getAuthentication() == null){
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                });
            }catch (ExpiredJwtException error){
                logger.error("Error while executing the doFilterInternal.");
                error.printStackTrace();
                throw new ValidationError(error.getMessage(), HttpStatus.UNAUTHORIZED);
            }catch (MalformedJwtException error){
                logger.error("Error while executing the doFilterInternal.");
                error.printStackTrace();
                throw new ValidationError(error.getMessage(),HttpStatus.UNAUTHORIZED);
            }catch (JwtException error){
                logger.error("Error while executing the doFilterInternal.");
                SecurityContextHolder.clearContext();
                error.printStackTrace();
                throw new ValidationError( "Invalid or expired token.",HttpStatus.UNAUTHORIZED);
            }catch (Exception error){
                logger.error("Error while executing the doFilterInternal.");
                error.printStackTrace();
                throw new ValidationError("Invalid error token.",HttpStatus.FORBIDDEN);
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/auth")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/oauth2/");
    }
}
