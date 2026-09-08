package com.auth.Auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Authentication API",
                description = """
                        Generic Authentication and Authorization API built with Spring Boot.

                        This application provides:
                        - User registration
                        - User login
                        - JWT-based authentication
                        - Access token and refresh token support
                        - HttpOnly cookie-based refresh tokens
                        - OAuth2 authentication with Google and GitHub
                        - Role-based authorization
                        - User management
                        """,
                version = "1.0.0",
                contact = @Contact(
                        name = "Anamuddin Ahmad",
                        email = "anamuddinahmad0786@gmail.com",
                        url = "https://link-tree-mauve.vercel.app/"
                ),
                license = @License(
                        name = "MIT License"
                )
        ),

        servers = {
                @Server(
                        url = "http://localhost:8084",
                        description = "Local Development Server"
                )
        },

        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Access Token. Enter the token in the format: Bearer {token}",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP
)
public class APIDocConfig {
}