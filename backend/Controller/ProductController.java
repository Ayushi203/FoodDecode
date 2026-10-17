package com.fooddecode.controller;

import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.product.IngredientItemResponse;
import com.fooddecode.dto.product.NutritionFactsResponse;
import com.fooddecode.dto.product.ProductDetailResponse;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.dto.product.ScanImageResponse;
import com.fooddecode.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public ApiResponse<List<ProductSummaryResponse>> search(
            @RequestParam(required = false) String query, @RequestParam(required = false) String category) {
        return ApiResponse.ok("Products", productService.search(query, category));
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.ok("Categories", productService.categories());
    }

    @GetMapping("/recommended")
    public ApiResponse<List<ProductSummaryResponse>> recommended() {
        return ApiResponse.ok("Recommended", productService.recommended());
    }

    @GetMapping("/barcode/{barcode}")
    public ApiResponse<ProductDetailResponse> barcode(@PathVariable String barcode) {
        return ApiResponse.ok("Product", productService.getByBarcode(barcode));
    }

    @GetMapping("/compare")
    public ApiResponse<List<ProductDetailResponse>> compare(@RequestParam String ids) {
        return ApiResponse.ok("Comparison", productService.compareAsMap(ids).values().stream().toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Product", productService.getById(id));
    }

    @GetMapping("/{id}/ingredients")
    public ApiResponse<List<IngredientItemResponse>> ingredients(@PathVariable Long id) {
        return ApiResponse.ok("Ingredients", productService.getIngredients(id));
    }

    @GetMapping("/{id}/nutrition")
    public ApiResponse<NutritionFactsResponse> nutrition(@PathVariable Long id) {
        return ApiResponse.ok("Nutrition", productService.getNutrition(id));
    }

    @GetMapping("/{id}/alternatives")
    public ApiResponse<List<ProductSummaryResponse>> alternatives(@PathVariable Long id) {
        List<ProductSummaryResponse> items = productService.alternatives(id);
        String message = items.isEmpty()
                ? "No comparable alternatives found yet"
                : "Healthier packs of the same kind";
        return ApiResponse.ok(message, items);
    }

    @PostMapping("/scan-image")
    public ApiResponse<ScanImageResponse> scanImage(@RequestPart(value = "file", required = false) MultipartFile file) {
        // TODO: AI image analysis
        return ApiResponse.ok("Scan image helper", productService.scanImage(file));
    }
}
