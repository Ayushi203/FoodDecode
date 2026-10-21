package com.fooddecode.dto.user;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDashboardResponse {

    private final Metric scansThisMonth;
    private final Metric avgHealthScore;
    private final Metric itemsFlagged;
    private final Metric favoritesSaved;
    private final List<WeekPoint> healthScoreTrend;
    private final List<CategoryCount> scansByCategory;
    private final int recallAlertCount;

    @Getter
    @Builder
    public static class Metric {
        private final Double value;
        private final Double previousValue;
        private final Double changePercent;
        private final String trendNote;
        private final List<Double> sparkline;
    }

    @Getter
    @Builder
    public static class WeekPoint {
        private final String label;
        private final Double averageScore;
        private final long scanCount;
    }

    @Getter
    @Builder
    public static class CategoryCount {
        private final String category;
        private final long count;
    }
}
