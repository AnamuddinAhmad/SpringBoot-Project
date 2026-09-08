package com.auth.Auth.exception;

import org.springframework.http.HttpStatus;

public class ValidationError extends RuntimeException {

    private final HttpStatus status;

    public ValidationError(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ValidationError badRequest(String message) {
        return new ValidationError(
                message,
                HttpStatus.BAD_REQUEST
        );
    }

    public static ValidationError unauthorized(String message) {
        return new ValidationError(
                message,
                HttpStatus.UNAUTHORIZED
        );
    }

    public static ValidationError forbidden(String message) {
        return new ValidationError(
                message,
                HttpStatus.FORBIDDEN
        );
    }

    public static ValidationError notFound(String message) {
        return new ValidationError(
                message,
                HttpStatus.NOT_FOUND
        );
    }

    public static ValidationError conflict(String message) {
        return new ValidationError(
                message,
                HttpStatus.CONFLICT
        );
    }

    public static ValidationError tooManyRequests(String message) {
        return new ValidationError(
                message,
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public static ValidationError internalServerError(String message) {
        return new ValidationError(
                message,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}