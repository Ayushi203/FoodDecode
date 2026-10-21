package com.fooddecode.service;

import com.fooddecode.dto.product.IngredientItemResponse;
import com.fooddecode.dto.product.NutritionFactsResponse;
import com.fooddecode.dto.product.ProductDetailResponse;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.dto.product.ScanImageResponse;
import com.fooddecode.entity.Ingredient;
import com.fooddecode.entity.NutritionFacts;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.ProductIngredient;
import com.fooddecode.entity.User;
import com.fooddecode.enums.DietPreference;
import com.fooddecode.enums.ProductSource;
import com.fooddecode.exception.ApiException;
import com.fooddecode.mapper.ProductMapper;
import com.fooddecode.repository.IngredientRepository;
import com.fooddecode.repository.NutritionFactsRepository;
import com.fooddecode.repository.ProductIngredientRepository;
import com.fooddecode.repository.ProductRepository;
import com.fooddecode.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final NutritionFactsRepository nutritionFactsRepository;
    private final OpenFoodFactsService openFoodFactsService;
    private final HealthScoreService healthScoreService;
    private final AllergenService allergenService;
    private final ReviewRepository reviewRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public List<ProductSummaryResponse> search(String query, String category) {
        List<ProductSummaryResponse> results;
        if (query == null || query.isBlank()) {
            results = productRepository.findAll().stream().map(ProductMapper::toSummary).toList();
        } else {
            List<Product> local =
                    productRepository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query);
            if (!local.isEmpty()) {
                results = local.stream().map(ProductMapper::toSummary).toList();
            } else {
                results = new ArrayList<>();
                for (OpenFoodFactsService.OffProduct remote : openFoodFactsService.search(query)) {
                    results.add(ProductMapper.toSummary(upsertFromOff(remote)));
                }
            }
        }
        if (category == null || category.isBlank()) {
            return results;
        }
        String wanted = CategoryTaxonomy.canonical("", "", category);
        return results.stream()
                .filter(item -> wanted.equals(
                        CategoryTaxonomy.canonical(item.getName(), item.getBrand(), item.getCategory())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findAll().stream()
                .map(p -> CategoryTaxonomy.canonical(p.getName(), p.getBrand(), p.getCategory()))
                .filter(c -> c != null && !c.isBlank() && !"other".equals(c) && !"unknown".equals(c))
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> recommended() {
        User user = currentUserService.requireUser();
        DietPreference diet = user.getDietPreference() == null ? DietPreference.NONE : user.getDietPreference();
        if (diet == DietPreference.NONE) {
            return List.of();
        }
        return productRepository.findAll().stream()
                .filter(p -> matchesDiet(p, diet))
                .sorted(Comparator.comparing((Product p) -> p.getHealthScore() == null ? 0 : p.getHealthScore()).reversed())
                .limit(12)
                .map(ProductMapper::toSummary)
                .toList();
    }

    @Transactional
    public ProductDetailResponse getByBarcode(String barcode) {
        String code = barcode == null ? "" : barcode.replaceAll("\\s+", "");
        if (code.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a barcode");
        }
        return productRepository.findByBarcode(code)
                .map(this::toDetail)
                .orElseGet(() -> {
                    OpenFoodFactsService.OffProduct remote = openFoodFactsService.fetchByBarcode(code);
                    if (remote == null) {
                        throw new ApiException(HttpStatus.NOT_FOUND, "No product for barcode " + code);
                    }
                    return toDetail(upsertFromOff(remote));
                });
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(Long id) {
        return toDetail(requireProduct(id));
    }

    @Transactional(readOnly = true)
    public List<IngredientItemResponse> getIngredients(Long id) {
        ProductDetailResponse detail = getById(id);
        return detail.getIngredients();
    }

    @Transactional(readOnly = true)
    public NutritionFactsResponse getNutrition(Long id) {
        NutritionFacts nutrition = nutritionFactsRepository.findByProductId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No nutrition facts for this product"));
        return ProductMapper.toNutrition(nutrition);
    }

    @Transactional(readOnly = true)
    public List<ProductDetailResponse> compare(List<Long> ids) {
        List<ProductDetailResponse> result = new ArrayList<>();
        for (Long id : ids) {
            result.add(getById(id));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> alternatives(Long id) {
        Product product = requireProduct(id);
        String canonical = CategoryTaxonomy.canonical(product.getName(), product.getBrand(), product.getCategory());
        List<String> comparable = CategoryTaxonomy.comparableCategories(canonical);
        if (comparable.isEmpty()) {
            return List.of();
        }
        int score = product.getHealthScore() == null ? 0 : product.getHealthScore();
        return productRepository
                .findByCategoryIgnoreCaseInAndHealthScoreGreaterThanAndIdNotOrderByHealthScoreDesc(
                        comparable, score, product.getId(), PageRequest.of(0, 6))
                .stream()
                .map(ProductMapper::toSummary)
                .toList();
    }

    /**
     * MVP helper: accept an image upload so the scanner page has a working endpoint.
     * // TODO: AI image analysis
     */
    public ScanImageResponse scanImage(MultipartFile file) {
        ScanImageResponse response = new ScanImageResponse();
        response.setMessage(file == null || file.isEmpty()
                ? "No image received. Enter a barcode manually or use the camera scanner."
                : "Image '" + file.getOriginalFilename() + "' received. Label OCR is not enabled yet.");
        response.setTodo("AI image analysis");
        return response;
    }

    public Product requireProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private ProductDetailResponse toDetail(Product product) {
        List<ProductIngredient> links = productIngredientRepository.findByProductIdOrderByPositionInListAsc(product.getId());
        NutritionFacts nutrition = nutritionFactsRepository.findByProductId(product.getId()).orElse(null);
        List<String> warnings = currentUserService.findUser()
                .map(user -> allergenService.warningsFor(product.getId(), user))
                .orElse(List.of());
        Double avg = reviewRepository.averageRating(product.getId());
        int reviewCount = (int) reviewRepository.countByProductId(product.getId());
        return ProductMapper.toDetail(product, links, nutrition).toBuilder()
                .allergyWarning(warnings)
                .averageRating(avg)
                .reviewCount(reviewCount)
                .build();
    }

    private boolean matchesDiet(Product product, DietPreference diet) {
        List<ProductIngredient> links =
                productIngredientRepository.findByProductIdOrderByPositionInListAsc(product.getId());
        String blob = ((product.getName() == null ? "" : product.getName()) + " "
                + (product.getBrand() == null ? "" : product.getBrand()) + " "
                + links.stream().map(pi -> pi.getIngredient().getName()).collect(java.util.stream.Collectors.joining(" ")))
                .toLowerCase(Locale.ROOT);
        NutritionFacts nutrition = nutritionFactsRepository.findByProductId(product.getId()).orElse(null);
        return switch (diet) {
            case LOW_SUGAR -> nutrition != null && nutrition.getSugarG() != null
                    && nutrition.getSugarG().doubleValue() <= 10
                    && product.getHealthScore() != null
                    && product.getHealthScore() >= 55;
            case HIGH_PROTEIN -> nutrition != null && nutrition.getProteinG() != null
                    && nutrition.getProteinG().doubleValue() >= 8
                    && product.getHealthScore() != null
                    && product.getHealthScore() >= 50;
            case VEGAN -> !containsAny(blob, "milk", "butter", "cheese", "whey", "honey", "egg", "gelatin", "chicken", "fish", "meat", "dahi", "yoghurt", "yogurt")
                    && product.getHealthScore() != null
                    && product.getHealthScore() >= 50;
            case VEGETARIAN -> !containsAny(blob, "chicken", "fish", "meat", "beef", "pork", "gelatin", "prawn", "shrimp")
                    && product.getHealthScore() != null
                    && product.getHealthScore() >= 45;
            case NONE -> false;
        };
    }

    private boolean containsAny(String blob, String... needles) {
        for (String needle : needles) {
            if (blob.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private Product upsertFromOff(OpenFoodFactsService.OffProduct remote) {
        Product product = remote.barcode() == null
                ? new Product()
                : productRepository.findByBarcode(remote.barcode()).orElseGet(Product::new);
        product.setBarcode(blankToNull(remote.barcode()));
        product.setName(remote.name());
        product.setBrand(remote.brand());
        product.setCategory(CategoryTaxonomy.canonical(remote.name(), remote.brand(), remote.category()));
        product.setImageUrl(remote.imageUrl());
        product.setSource(ProductSource.OPEN_FOOD_FACTS);
        product = productRepository.save(product);
        List<ProductIngredient> links = replaceIngredients(product, remote.ingredientNames());

        NutritionFacts nutrition = nutritionFactsRepository.findByProductId(product.getId()).orElseGet(NutritionFacts::new);
        nutrition.setProduct(product);
        if (remote.nutrition() != null) {
            nutrition.setCalories(remote.nutrition().calories());
            nutrition.setProteinG(remote.nutrition().proteinG());
            nutrition.setCarbsG(remote.nutrition().carbsG());
            nutrition.setFatsG(remote.nutrition().fatsG());
            nutrition.setSugarG(remote.nutrition().sugarG());
            nutrition.setSodiumMg(remote.nutrition().sodiumMg());
            nutrition.setFibreG(remote.nutrition().fibreG());
            nutrition.setServingSize(remote.nutrition().servingSize());
        }
        nutritionFactsRepository.save(nutrition);
        product.setHealthScore(healthScoreService.calculate(product, links, nutrition));
        return productRepository.save(product);
    }

    /**
     * Rebuilds the join rows for one product. Open Food Facts often repeats the same
     * ingredient (nested tree + flat list, or the same name with different casing).
     * The composite key is (product_id, ingredient_id), so a second save of the same
     * pair in this session triggers Hibernate's "a different object with the same
     * identifier value was already associated with the session" error.
     *
     * We clear the persistent collection and flush so orphanRemoval deletes old rows
     * before inserting replacements — a bulk deleteByProductId would leave the old
     * ProductIngredient instances in the persistence context.
     */
    private List<ProductIngredient> replaceIngredients(Product product, List<String> rawNames) {
        product.getProductIngredients().clear();
        productRepository.flush();

        List<ProductIngredient> links = new ArrayList<>();
        Set<Long> seenIngredientIds = new HashSet<>();
        int position = 1;
        if (rawNames == null) {
            return links;
        }
        for (String rawName : rawNames) {
            if (rawName == null || rawName.isBlank()) {
                continue;
            }
            Ingredient ingredient = findOrCreateIngredient(rawName.trim());
            if (ingredient.getId() != null && !seenIngredientIds.add(ingredient.getId())) {
                continue;
            }
            ProductIngredient link = new ProductIngredient();
            link.setProduct(product);
            link.setIngredient(ingredient);
            link.setPositionInList(position++);
            product.getProductIngredients().add(link);
            links.add(link);
        }
        return links;
    }

    private Ingredient findOrCreateIngredient(String name) {
        return ingredientRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Ingredient created = new Ingredient();
            created.setName(name);
            HarmHint hint = classify(name);
            created.setHarmful(hint.harmful());
            created.setHarmCategory(hint.category());
            return ingredientRepository.save(created);
        });
    }

    private HarmHint classify(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("hydrogenated") || n.contains("trans fat") || n.contains("palm oil")) {
            return new HarmHint(true, "trans_fat");
        }
        if (n.contains("aspartame") || n.contains("sucralose") || n.contains("saccharin") || n.contains("acesulfame")) {
            return new HarmHint(true, "artificial_sweetener");
        }
        if (n.contains("tbhq") || n.contains("bha") || n.contains("bht") || n.contains("sodium nitrite")
                || n.contains("phosphoric")) {
            return new HarmHint(true, "preservative");
        }
        if (n.contains("high fructose") || n.contains("corn syrup")) {
            return new HarmHint(true, "added_sugar");
        }
        if (n.contains("monosodium glutamate") || n.contains("msg")) {
            return new HarmHint(true, "additive");
        }
        return new HarmHint(false, null);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record HarmHint(boolean harmful, String category) {}

    public Map<String, ProductDetailResponse> compareAsMap(String ids) {
        List<Long> parsed = new ArrayList<>();
        for (String part : ids.split(",")) {
            if (!part.isBlank()) {
                parsed.add(Long.parseLong(part.trim()));
            }
        }
        Map<String, ProductDetailResponse> map = new LinkedHashMap<>();
        int i = 1;
        for (ProductDetailResponse item : compare(parsed)) {
            map.put("product" + i++, item);
        }
        return map;
    }
}
