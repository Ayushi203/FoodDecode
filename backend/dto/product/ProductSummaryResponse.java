package com.fooddecode.dto.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSummaryResponse {

    private final Long id;
    private final String barcode;
    private final String name;
    private final String brand;
    private final String category;
    private final String imageUrl;
    private final Integer healthScore;
    private final boolean recalled;
    private final String recallReason;
    private final String purchaseUrl;
}
