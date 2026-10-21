package com.fooddecode.dto.product;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NutritionFactsResponse {

    private final BigDecimal calories;
    private final BigDecimal proteinG;
    private final BigDecimal carbsG;
    private final BigDecimal fatsG;
    private final BigDecimal sugarG;
    private final BigDecimal sodiumMg;
    private final BigDecimal fibreG;
    private final String servingSize;
}
