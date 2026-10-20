package com.fooddecode.dto.allergen;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AllergenResponse {
    private final Long id;
    private final String name;
}
