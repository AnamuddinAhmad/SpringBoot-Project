package com.auth.Auth.services.Implementation;

import com.auth.Auth.entity.OtpToken;
import com.auth.Auth.entity.Users;
import com.auth.Auth.exception.ValidationError;
import com.auth.Auth.repo.OtpTokenRepo;
import com.auth.Auth.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_SECONDS = 600; // 10 minutes
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepo otpTokenRepo;
    private final UserRepo userRepo;
    private final MailService mailService;

    @Transactional
    public void generateAndSendOtp(String email) {
        // Invalidate any previous unused OTPs for this email first.
        otpTokenRepo.deleteByEmail(email);

        String code = generateCode();

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .code(code)
                .expiresAt(Instant.now().plusSeconds(OTP_TTL_SECONDS))
                .used(false)
                .createdAt(Instant.now())
                .build();

        otpTokenRepo.save(otpToken);
        mailService.sendOtpEmail(email, code);
    }

    @Transactional
    public void verifyOtp(String email, String submittedCode) {
        OtpToken otpToken = otpTokenRepo
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> ValidationError.badRequest("No verification code found for this email."));

        if (otpToken.getExpiresAt().isBefore(Instant.now())) {
            throw ValidationError.badRequest("Verification code has expired. Please request a new one.");
        }

        if (!otpToken.getCode().equals(submittedCode)) {
            throw ValidationError.badRequest("Invalid verification code.");
        }

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> ValidationError.notFound("User not found."));

        user.setEnabled(true);
        userRepo.save(user);

        otpToken.setUsed(true);
        otpTokenRepo.save(otpToken);
    }

    /**
     * Generates a zero-padded numeric OTP of length OTP_LENGTH (e.g. "004821").
     */
    private String generateCode() {
        int max = (int) Math.pow(10, OTP_LENGTH);
        int number = RANDOM.nextInt(max);
        return String.format("%0" + OTP_LENGTH + "d", number);
    }
}