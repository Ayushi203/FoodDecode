package com.fooddecode.service;

import com.fooddecode.dto.admin.AdminIngredientRequest;
import com.fooddecode.dto.admin.AdminProductRequest;
import com.fooddecode.dto.admin.AnalyticsResponse;
import com.fooddecode.dto.product.IngredientItemResponse;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.entity.Ingredient;
import com.fooddecode.entity.Product;
import com.fooddecode.enums.ProductSource;
import com.fooddecode.exception.ApiException;
import com.fooddecode.mapper.ProductMapper;
import com.fooddecode.repository.IngredientRepository;
import com.fooddecode.repository.ProductIngredientRepository;
import com.fooddecode.repository.ProductRepository;
import com.fooddecode.repository.ScanHistoryRepository;
import com.fooddecode.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final UserRepository userRepository;
    private final ScanHistoryRepository scanHistoryRepository;

    @Transactional
    public ProductSummaryResponse createProduct(AdminProductRequest request) {
        Product product = new Product();
        apply(product, request);
        product.setSource(ProductSource.ADMIN_ADDED);
        return ProductMapper.toSummary(productRepository.save(product));
    }

    @Transactional
    public ProductSummaryResponse updateProduct(Long id, AdminProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        apply(product, request);
        return ProductMapper.toSummary(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public IngredientItemResponse createIngredient(AdminIngredientRequest request) {
        if (ingredientRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ingredient already exists");
        }
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName().trim());
        ingredient.setHarmful(request.isHarmful());
        ingredient.setHarmCategory(request.getHarmCategory());
        ingredient = ingredientRepository.save(ingredient);
        return toIngredient(ingredient);
    }

    @Transactional
    public IngredientItemResponse updateIngredient(Long id, AdminIngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ingredient not found"));
        ingredient.setName(request.getName().trim());
        ingredient.setHarmful(request.isHarmful());
        ingredient.setHarmCategory(request.getHarmCategory());
        return toIngredient(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Ingredient not found");
        }
        ingredientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse analytics() {
        List<AnalyticsResponse.NamedCount> scanned = scanHistoryRepository
                .findMostScannedProducts(PageRequest.of(0, 5))
                .stream()
                .map(row -> AnalyticsResponse.NamedCount.builder()
                        .id((Long) row[0])
                        .name((String) row[1])
                        .count((Long) row[2])
                        .build())
                .toList();
        List<AnalyticsResponse.NamedCount> harmful = productIngredientRepository
                .findMostFlaggedHarmfulIngredients(PageRequest.of(0, 5))
                .stream()
                .map(row -> AnalyticsResponse.NamedCount.builder()
                        .id((Long) row[0])
                        .name((String) row[1])
                        .extra((String) row[2])
                        .count((Long) row[3])
                        .build())
                .toList();
        return AnalyticsResponse.builder()
                .totalUsers(userRepository.count())
                .totalScans(scanHistoryRepository.count())
                .mostScannedProducts(scanned)
                .mostFlaggedHarmfulIngredients(harmful)
                .build();
    }

    private void apply(Product product, AdminProductRequest request) {
        product.setBarcode(blankToNull(request.getBarcode()));
        product.setName(request.getName().trim());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setPurchaseUrl(blankToNull(request.getPurchaseUrl()));
    }

    private IngredientItemResponse toIngredient(Ingredient ingredient) {
        return IngredientItemResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .harmful(ingredient.isHarmful())
                .harmCategory(ingredient.getHarmCategory())
                .positionInList(0)
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
