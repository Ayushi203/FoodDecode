package com.fooddecode.controller;

import com.fooddecode.dto.admin.AdminIngredientRequest;
import com.fooddecode.dto.admin.AdminProductRequest;
import com.fooddecode.dto.admin.AnalyticsResponse;
import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.product.IngredientItemResponse;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.repository.IngredientRepository;
import com.fooddecode.repository.ProductRepository;
import com.fooddecode.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final com.fooddecode.service.RecallService recallService;

    @GetMapping("/products")
    public ApiResponse<List<ProductSummaryResponse>> products() {
        return ApiResponse.ok(
                "Products",
                productRepository.findAll().stream()
                        .map(com.fooddecode.mapper.ProductMapper::toSummary)
                        .toList());
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductSummaryResponse> createProduct(@Valid @RequestBody AdminProductRequest request) {
        return ApiResponse.ok("Product created", adminService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<ProductSummaryResponse> updateProduct(
            @PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        return ApiResponse.ok("Product updated", adminService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ApiResponse.ok("Product deleted");
    }

    @GetMapping("/ingredients")
    public ApiResponse<List<IngredientItemResponse>> ingredients() {
        return ApiResponse.ok(
                "Ingredients",
                ingredientRepository.findAll().stream()
                        .map(i -> IngredientItemResponse.builder()
                                .id(i.getId())
                                .name(i.getName())
                                .harmful(i.isHarmful())
                                .harmCategory(i.getHarmCategory())
                                .positionInList(0)
                                .build())
                        .toList());
    }

    @PostMapping("/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IngredientItemResponse> createIngredient(@Valid @RequestBody AdminIngredientRequest request) {
        return ApiResponse.ok("Ingredient created", adminService.createIngredient(request));
    }

    @PutMapping("/ingredients/{id}")
    public ApiResponse<IngredientItemResponse> updateIngredient(
            @PathVariable Long id, @Valid @RequestBody AdminIngredientRequest request) {
        return ApiResponse.ok("Ingredient updated", adminService.updateIngredient(id, request));
    }

    @DeleteMapping("/ingredients/{id}")
    public ApiResponse<Void> deleteIngredient(@PathVariable Long id) {
        adminService.deleteIngredient(id);
        return ApiResponse.ok("Ingredient deleted");
    }

    @GetMapping("/analytics")
    public ApiResponse<AnalyticsResponse> analytics() {
        return ApiResponse.ok("Analytics", adminService.analytics());
    }

    @PostMapping("/products/{id}/recall")
    public ApiResponse<ProductSummaryResponse> recall(
            @PathVariable Long id, @RequestBody(required = false) com.fooddecode.dto.admin.RecallRequest request) {
        return ApiResponse.ok(
                "Product recalled",
                recallService.flag(id, request == null ? new com.fooddecode.dto.admin.RecallRequest() : request));
    }

    @PostMapping("/products/{id}/recall/clear")
    public ApiResponse<ProductSummaryResponse> clearRecall(@PathVariable Long id) {
        return ApiResponse.ok("Recall cleared", recallService.clear(id));
    }
}
