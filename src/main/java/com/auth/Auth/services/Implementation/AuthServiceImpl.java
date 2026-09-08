package com.auth.Auth.services.Implementation;

import com.auth.Auth.dto.UserDTO;
import com.auth.Auth.services.AuthServices;
import com.auth.Auth.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthServices {

    private final UserServices userServices;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Override
    public UserDTO registerUser(UserDTO user) {

        // Local self-service registration always starts unverified,
        // regardless of what the client sent for `enabled`.
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        UserDTO createdUser = userServices.createUser(user);

        otpService.generateAndSendOtp(createdUser.getEmail());

        return createdUser;
    }
}