package com.auth.Auth.repo;

import com.auth.Auth.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepo extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}