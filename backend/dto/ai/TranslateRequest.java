package com.fooddecode.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslateRequest {

    @NotBlank
    @Size(max = 4000)
    private String text;

    @NotBlank
    @Size(max = 20)
    private String targetLanguage;
}
