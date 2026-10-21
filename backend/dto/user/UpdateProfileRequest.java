package com.fooddecode.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @Size(max = 10)
    private String preferredLanguage;
}
