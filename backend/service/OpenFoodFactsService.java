package com.fooddecode.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class OpenFoodFactsService {

    private final RestClient restClient;
    private final String baseUrl;

    public OpenFoodFactsService(
            RestClient restClient, @Value("${open-food-facts.base-url:https://world.openfoodfacts.org}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public OffProduct fetchByBarcode(String barcode) {
        try {
            JsonNode root = restClient.get()
                    .uri(baseUrl + "/api/v2/product/{barcode}.json", barcode)
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null || root.path("status").asInt() != 1) {
                return null;
            }
            return map(root.path("product"), barcode);
        } catch (RestClientException ex) {
            return null;
        }
    }

    public List<OffProduct> search(String query) {
        try {
            JsonNode root = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("world.openfoodfacts.org")
                            .path("/cgi/search.pl")
                            .queryParam("search_terms", query)
                            .queryParam("search_simple", "1")
                            .queryParam("action", "process")
                            .queryParam("json", "1")
                            .queryParam("page_size", "8")
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            List<OffProduct> results = new ArrayList<>();
            if (root == null) {
                return results;
            }
            for (JsonNode node : root.path("products")) {
                String code = text(node, "code");
                OffProduct mapped = map(node, code);
                if (mapped != null && mapped.name() != null && !mapped.name().isBlank()) {
                    results.add(mapped);
                }
            }
            return results;
        } catch (RestClientException ex) {
            return List.of();
        }
    }

    private OffProduct map(JsonNode product, String barcode) {
        if (product == null || product.isMissingNode()) {
            return null;
        }
        String name = firstNonBlank(text(product, "product_name"), text(product, "product_name_en"), "Unknown product");
        String brand = firstNonBlank(text(product, "brands"), null);
        String category = CategoryTaxonomy.canonical(name, brand, firstCategory(text(product, "categories_tags"), text(product, "categories")));
        String image = firstNonBlank(
                text(product, "image_front_url"),
                text(product, "image_url"),
                text(product, "image_front_small_url"),
                text(product, "image_small_url"));
        if (image != null && image.startsWith("http://")) {
            image = "https://" + image.substring(7);
        }
        List<String> ingredients = parseIngredients(product);
        JsonNode nutriments = product.path("nutriments");
        Nutrition n = new Nutrition(
                decimal(nutriments, "energy-kcal_100g", "energy-kcal"),
                decimal(nutriments, "proteins_100g", "proteins"),
                decimal(nutriments, "carbohydrates_100g", "carbohydrates"),
                decimal(nutriments, "fat_100g", "fat"),
                decimal(nutriments, "sugars_100g", "sugars"),
                sodiumMg(nutriments),
                decimal(nutriments, "fiber_100g", "fiber"),
                firstNonBlank(text(product, "serving_size"), "100 g"));
        return new OffProduct(barcode, name, brand, category, image, ingredients, n);
    }

    private List<String> parseIngredients(JsonNode product) {
        LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        JsonNode array = product.path("ingredients");
        if (array.isArray()) {
            for (JsonNode item : array) {
                String name = firstNonBlank(text(item, "text"), text(item, "id"));
                addUniqueIngredient(unique, name);
            }
        }
        if (unique.isEmpty()) {
            String text = text(product, "ingredients_text");
            if (text != null) {
                for (String part : text.split("[,;]")) {
                    addUniqueIngredient(unique, part);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private void addUniqueIngredient(LinkedHashMap<String, String> unique, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        String cleaned = cleanIngredient(raw);
        if (cleaned.isBlank()) {
            return;
        }
        unique.putIfAbsent(cleaned.toLowerCase(Locale.ROOT), cleaned);
    }

    private String cleanIngredient(String raw) {
        String value = raw.replace("en:", "").replace("_", " ").trim();
        if (value.length() > 120) {
            value = value.substring(0, 120);
        }
        return value;
    }

    private String firstCategory(String tags, String categories) {
        if (tags != null && !tags.isBlank()) {
            String first = tags.split(",")[0].trim();
            return first.replace("en:", "").replace("-", "_");
        }
        if (categories != null && !categories.isBlank()) {
            return categories.split(",")[0].trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        }
        return "unknown";
    }

    private BigDecimal sodiumMg(JsonNode nutriments) {
        BigDecimal mg = decimal(nutriments, "sodium_100g");
        if (mg != null) {
            // Open Food Facts stores sodium_100g in grams.
            return mg.multiply(BigDecimal.valueOf(1000));
        }
        return decimal(nutriments, "sodium");
    }

    private BigDecimal decimal(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isNumber()) {
                return value.decimalValue();
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public record OffProduct(
            String barcode,
            String name,
            String brand,
            String category,
            String imageUrl,
            List<String> ingredientNames,
            Nutrition nutrition) {}

    public record Nutrition(
            BigDecimal calories,
            BigDecimal proteinG,
            BigDecimal carbsG,
            BigDecimal fatsG,
            BigDecimal sugarG,
            BigDecimal sodiumMg,
            BigDecimal fibreG,
            String servingSize) {}
}
