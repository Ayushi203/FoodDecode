package com.fooddecode.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminIngredientRequest {

    @NotBlank
    private String name;

    private boolean harmful;
    private String harmCategory;
}
