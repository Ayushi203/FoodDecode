package com.fooddecode.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    private String resetToken;

    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;
}
