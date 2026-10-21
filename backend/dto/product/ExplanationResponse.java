package com.fooddecode.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExplanationResponse {

    private final Long ingredientId;
    private final String languageCode;
    private final String simpleExplanation;
    private final boolean aiGenerated;
}
