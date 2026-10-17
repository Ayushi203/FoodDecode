package com.fooddecode.controller;

import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.review.ReviewRequest;
import com.fooddecode.dto.review.ReviewResponse;
import com.fooddecode.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    public ApiResponse<List<ReviewResponse>> list(@PathVariable Long productId) {
        return ApiResponse.ok("Reviews", reviewService.list(productId));
    }

    @PostMapping("/api/products/{productId}/reviews")
    public ApiResponse<ReviewResponse> upsert(
            @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok("Review saved", reviewService.upsert(productId, request));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ApiResponse.ok("Review deleted");
    }
}
