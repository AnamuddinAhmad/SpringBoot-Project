package com.auth.Auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "ROLES")
public class Roles {
    @Id
    @Column(name = "roles_id")
    private UUID id = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String name;
}
