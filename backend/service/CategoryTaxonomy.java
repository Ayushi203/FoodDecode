package com.fooddecode.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Maps messy Open Food Facts tags onto the packed-food types this app actually compares.
 */
public final class CategoryTaxonomy {

    private static final Set<String> KNOWN = Set.of(
            "instant_noodles",
            "seasoning",
            "snacks",
            "chips",
            "biscuits",
            "cookies",
            "breakfast_cereal",
            "soft_drink",
            "juice",
            "water",
            "dairy",
            "yoghurt",
            "yogurt",
            "spreads");

    private CategoryTaxonomy() {}

    public static String canonical(String name, String brand, String rawCategory) {
        String blob = ((name == null ? "" : name) + " " + (brand == null ? "" : brand) + " " + (rawCategory == null ? "" : rawCategory))
                .toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replace('_', ' ');

        if (containsAny(blob, "masala magic", "masala ae magic", "masala-ae-magic", "seasoning", "spice mix", "kitchen king")) {
            if (!containsAny(blob, "noodle", "ramen")) {
                return "seasoning";
            }
        }
        if (containsAny(blob, "noodle", "ramen", "yippee", "cup noodles")) {
            return "instant_noodles";
        }
        if (containsAny(blob, "biscuit", "cookie", "marie", "oreo", "parle g", "good day", "digestive")) {
            return "biscuits";
        }
        if (containsAny(blob, "chips", "crisp", "namkeen", "wafer snack")) {
            return "snacks";
        }
        if (containsAny(blob, "oat", "muesli", "cereal", "cornflake")) {
            return "breakfast_cereal";
        }
        if (containsAny(blob, "cola", "soda", "soft drink", "fanta", "sprite", "pepsi")) {
            return "soft_drink";
        }
        if (containsAny(blob, "juice", "nectar", "coconut water")) {
            return "juice";
        }
        if (containsAny(blob, "milk", "dahi", "curd", "yoghurt", "yogurt", "cheese") && !containsAny(blob, "chocolate", "biscuit")) {
            return "dairy";
        }
        if (containsAny(blob, "spread", "nutella", "peanut butter", "jam")) {
            return "spreads";
        }

        if (rawCategory != null && !rawCategory.isBlank()) {
            String key = rawCategory.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
            if ("noodles".equals(key) || "pasta".equals(key)) {
                return "instant_noodles";
            }
            if ("appetizers".equals(key) || "chips".equals(key)) {
                return "snacks";
            }
            if (key.contains("biscuit") || "cookies".equals(key) || "confectioneries".equals(key)) {
                return "biscuits";
            }
            if (key.contains("breakfast") || key.contains("petit")) {
                return "breakfast_cereal";
            }
            if (KNOWN.contains(key)) {
                return key;
            }
        }
        return "other";
    }

    /**
     * Instant noodles may swap with masala/seasoning sachets; chips with snacks; biscuits with cookies.
     * Nothing else is treated as interchangeable.
     */
    public static List<String> comparableCategories(String category) {
        String key = category == null ? "" : category.toLowerCase(Locale.ROOT);
        if (Set.of("instant_noodles", "seasoning", "noodles").contains(key)) {
            return List.of("instant_noodles", "seasoning", "noodles");
        }
        if (Set.of("snacks", "chips").contains(key)) {
            return List.of("snacks", "chips");
        }
        if (Set.of("biscuits", "cookies").contains(key)) {
            return List.of("biscuits", "cookies");
        }
        if (Set.of("dairy", "yoghurt", "yogurt").contains(key)) {
            return List.of("dairy", "yoghurt", "yogurt");
        }
        if (key.isBlank() || "other".equals(key) || "unknown".equals(key)) {
            return List.of();
        }
        return List.of(key);
    }

    private static boolean containsAny(String blob, String... needles) {
        for (String needle : needles) {
            if (blob.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
