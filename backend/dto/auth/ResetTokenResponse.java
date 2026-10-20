package com.fooddecode.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetTokenResponse {

    private final String resetToken;
}
