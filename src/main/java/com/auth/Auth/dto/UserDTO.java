package com.auth.Auth.dto;

import com.auth.Auth.utils.Provider;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Builder.Default
    private Provider provider = Provider.LOCAL;

    @Builder.Default
    private Set<RolesDTO> roles = new HashSet<>();
}