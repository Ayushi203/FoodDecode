package com.fooddecode.dto.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IngredientItemResponse {

    private final Long id;
    private final String name;
    private final boolean harmful;
    private final String harmCategory;
    private final int positionInList;
}
