package com.fooddecode.dto.product;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ProductDetailResponse {

    private final Long id;
    private final String barcode;
    private final String name;
    private final String brand;
    private final String category;
    private final String imageUrl;
    private final String source;
    private final Integer healthScore;
    private final List<IngredientItemResponse> ingredients;
    private final NutritionFactsResponse nutrition;
    private final java.util.List<String> allergyWarning;
    private final boolean recalled;
    private final String recallReason;
    private final String purchaseUrl;
    private final Double averageRating;
    private final Integer reviewCount;
}
