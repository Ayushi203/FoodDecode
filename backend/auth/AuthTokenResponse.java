package com.fooddecode.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final String role;
    private final String fullName;
    private final String email;
}
