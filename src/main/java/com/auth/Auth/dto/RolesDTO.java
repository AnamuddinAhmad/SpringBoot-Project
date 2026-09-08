package com.auth.Auth.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolesDTO {
    private UUID id;
    private String name;
}
