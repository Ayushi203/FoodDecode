package com.fooddecode.service;

import com.fooddecode.dto.user.UserDashboardResponse;
import com.fooddecode.dto.user.UserDashboardResponse.CategoryCount;
import com.fooddecode.dto.user.UserDashboardResponse.Metric;
import com.fooddecode.dto.user.UserDashboardResponse.WeekPoint;
import com.fooddecode.entity.Favorite;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.ProductIngredient;
import com.fooddecode.entity.ScanHistory;
import com.fooddecode.entity.User;
import com.fooddecode.repository.FavoriteRepository;
import com.fooddecode.repository.ProductIngredientRepository;
import com.fooddecode.repository.ScanHistoryRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDashboardService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter WEEK_LABEL = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    private final CurrentUserService currentUserService;
    private final ScanHistoryRepository scanHistoryRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final RecallService recallService;

    @Transactional(readOnly = true)
    public UserDashboardResponse load() {
        User user = currentUserService.requireUser();
        List<ScanHistory> scans = scanHistoryRepository.findByUserOrderByScannedAtAsc(user);
        List<Favorite> favorites = favoriteRepository.findByUserOrderByAddedAtDesc(user);

        YearMonth thisMonth = YearMonth.now(ZONE);
        Instant thisStart = thisMonth.atDay(1).atStartOfDay(ZONE).toInstant();
        Instant nextStart = thisMonth.plusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant();
        Instant lastStart = thisMonth.minusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant();

        Map<Long, Integer> flagCache = new HashMap<>();

        List<ScanHistory> scansThis = inRange(scans, thisStart, nextStart);
        List<ScanHistory> scansLast = inRange(scans, lastStart, thisStart);

        double flagsThis = flags(scansThis, flagCache);
        double flagsLast = flags(scansLast, flagCache);
        Double avgThis = averageScore(scansThis);
        Double avgLast = averageScore(scansLast);

        long favThis = favorites.stream().filter(f -> in(f.getAddedAt(), thisStart, nextStart)).count();

        LocalDate weekCursor = LocalDate.now(ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Double> scanSpark = new ArrayList<>();
        List<Double> scoreSpark = new ArrayList<>();
        List<Double> flagSpark = new ArrayList<>();
        List<Double> favSpark = new ArrayList<>();
        List<WeekPoint> trend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate start = weekCursor.minusWeeks(i);
            LocalDate end = start.plusWeeks(1);
            Instant a = start.atStartOfDay(ZONE).toInstant();
            Instant b = end.atStartOfDay(ZONE).toInstant();
            List<ScanHistory> weekScans = inRange(scans, a, b);
            Double avg = averageScore(weekScans);
            long count = weekScans.size();
            trend.add(WeekPoint.builder()
                    .label(start.format(WEEK_LABEL))
                    .averageScore(avg)
                    .scanCount(count)
                    .build());
            if (i < 8) {
                scanSpark.add((double) count);
                scoreSpark.add(avg);
                flagSpark.add(flags(weekScans, flagCache));
                favSpark.add((double) favorites.stream().filter(f -> in(f.getAddedAt(), a, b)).count());
            }
        }

        Map<String, Long> byCat = new LinkedHashMap<>();
        for (ScanHistory scan : scans) {
            Product product = scan.getProduct();
            String cat = CategoryTaxonomy.canonical(product.getName(), product.getBrand(), product.getCategory());
            if (cat == null || cat.isBlank() || "other".equals(cat) || "unknown".equals(cat)) {
                cat = "other";
            }
            byCat.merge(labelCat(cat), 1L, Long::sum);
        }
        List<CategoryCount> categories = byCat.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(e -> CategoryCount.builder().category(e.getKey()).count(e.getValue()).build())
                .toList();

        return UserDashboardResponse.builder()
                .scansThisMonth(metric((double) scansThis.size(), (double) scansLast.size(), scanSpark))
                .avgHealthScore(metric(avgThis, avgLast, scoreSpark))
                .itemsFlagged(metric(flagsThis, flagsLast, flagSpark))
                .favoritesSaved(metric(
                        (double) favorites.size(),
                        (double) Math.max(0, favorites.size() - favThis),
                        favSpark))
                .healthScoreTrend(trend)
                .scansByCategory(categories)
                .recallAlertCount(recallService.mine().size())
                .build();
    }

    private Metric metric(Double current, Double previous, List<Double> spark) {
        Double pct = null;
        String note;
        if (current == null) {
            note = "No scored packs in this period";
        } else if (previous == null || previous == 0) {
            note = "Not enough last-month data to trend";
        } else {
            pct = Math.round(((current - previous) / previous) * 1000.0) / 10.0;
            note = "vs last month";
        }
        return Metric.builder()
                .value(current)
                .previousValue(previous)
                .changePercent(pct)
                .trendNote(note)
                .sparkline(spark)
                .build();
    }

    private List<ScanHistory> inRange(List<ScanHistory> scans, Instant start, Instant end) {
        return scans.stream().filter(s -> in(s.getScannedAt(), start, end)).toList();
    }

    private boolean in(Instant at, Instant start, Instant end) {
        return at != null && !at.isBefore(start) && at.isBefore(end);
    }

    private Double averageScore(List<ScanHistory> scans) {
        int n = 0;
        double sum = 0;
        for (ScanHistory scan : scans) {
            Integer score = scan.getProduct().getHealthScore();
            if (score != null) {
                sum += score;
                n++;
            }
        }
        if (n == 0) {
            return null;
        }
        return Math.round((sum / n) * 10.0) / 10.0;
    }

    private double flags(List<ScanHistory> scans, Map<Long, Integer> cache) {
        double total = 0;
        for (ScanHistory scan : scans) {
            Long id = scan.getProduct().getId();
            total += cache.computeIfAbsent(id, this::harmfulCount);
        }
        return total;
    }

    private int harmfulCount(Long productId) {
        return (int) productIngredientRepository.findByProductIdOrderByPositionInListAsc(productId).stream()
                .map(ProductIngredient::getIngredient)
                .filter(i -> i != null && i.isHarmful())
                .count();
    }

    private String labelCat(String value) {
        return value.replace('_', ' ');
    }
}
