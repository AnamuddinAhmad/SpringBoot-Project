package com.auth.Auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expireIn,
        String tokenType,
        UserDTO userdto
) {


    public static TokenResponse of(String accessToken, String refreshToken, long expireIn, UserDTO userdto){
        return new TokenResponse(accessToken, refreshToken, expireIn, "Bearer ", userdto);
    }
}
