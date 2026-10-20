package com.fooddecode.dto.cart;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartAnalysisResponse {
    private final int itemCount;
    private final int totalUnits;
    private final Double averageHealthScore;
    private final int harmfulFlagCount;
    private final String verdict;
}
