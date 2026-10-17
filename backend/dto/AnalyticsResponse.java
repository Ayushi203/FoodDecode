package com.fooddecode.dto.admin;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyticsResponse {

    private final long totalUsers;
    private final long totalScans;
    private final List<NamedCount> mostScannedProducts;
    private final List<NamedCount> mostFlaggedHarmfulIngredients;

    @Getter
    @Builder
    public static class NamedCount {
        private final Long id;
        private final String name;
        private final String extra;
        private final long count;
    }
}
