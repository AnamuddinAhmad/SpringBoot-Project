package com.auth.Auth.security;

import com.auth.Auth.exception.ValidationError;
import com.auth.Auth.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        System.out.println(
                Instant.now() +
                        " : CustomUserDetailService : loadUserByUsername -> " +
                        username
        );
        return userRepo.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("User not found."));
    }
}
