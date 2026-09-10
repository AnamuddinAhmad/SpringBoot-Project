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
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTServices jwtServices;
    private final UserRepo userRepo;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final ObjectMapper objectMapper; // inject this - Jackson's ObjectMapper

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer")) {
            try {
                String token = header.substring(7);
                if (!jwtServices.isAccessToken(token)) {
                    sendError(response, HttpStatus.UNAUTHORIZED, "Invalid access token.");
                    return; // stop the chain here
                }

                Jws<Claims> parse = jwtServices.parse(token);
                Claims payload = parse.getPayload();
                String userId = payload.getSubject();
                UUID id = UserHelper.parseUUID(userId);

                var userOpt = userRepo.findById(id);
                if (userOpt.isEmpty()) {
                    sendError(response, HttpStatus.UNAUTHORIZED, "User not found.");
                    return;
                }

                var user = userOpt.get();
                if (!user.isEnabled()) {
                    sendError(response, HttpStatus.UNAUTHORIZED, "Account is not enabled.");
                    return;
                }

                List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles()
                        .stream().map(roles -> new SimpleGrantedAuthority(roles.getName())).collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (ExpiredJwtException error) {
                logger.warn("Expired JWT: {}", error.getMessage());
                sendError(response, HttpStatus.UNAUTHORIZED, "Token expired.");
                return;
            } catch (MalformedJwtException error) {
                logger.warn("Malformed JWT: {}", error.getMessage());
                sendError(response, HttpStatus.UNAUTHORIZED, "Malformed token.");
                return;
            } catch (JwtException error) {
                logger.warn("Invalid JWT: {}", error.getMessage());
                SecurityContextHolder.clearContext();
                sendError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
                return;
            } catch (Exception error) {
                logger.error("Unexpected error in JwtAuthenticationFilter", error);
                sendError(response, HttpStatus.FORBIDDEN, "Authentication failed.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of(
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message
                ))
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/auth")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/oauth2/");
    }
}
