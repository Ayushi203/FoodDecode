package com.fooddecode.dto.recall;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecallAlertResponse {
    private final Long productId;
    private final String name;
    private final String brand;
    private final String recallReason;
    private final boolean inFavorites;
    private final boolean inCart;
    private final boolean inHistory;
}
