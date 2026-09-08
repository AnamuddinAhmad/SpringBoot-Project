package com.auth.Auth.dto;

import com.auth.Auth.utils.Provider;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUser {
    private String name;
    private String password;
    private String image;
    private Provider provider;
    private Boolean enabled;

}
