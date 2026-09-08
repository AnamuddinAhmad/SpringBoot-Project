package com.auth.Auth.controller;

import com.auth.Auth.dto.ResendOtpRequest;
import com.auth.Auth.dto.VerifyOtpRequest;
import com.auth.Auth.services.Implementation.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.ok(Map.of("message", "Account verified successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody ResendOtpRequest request) {
        otpService.generateAndSendOtp(request.email());
        return ResponseEntity.ok(Map.of("message", "Verification code resent."));
    }
}