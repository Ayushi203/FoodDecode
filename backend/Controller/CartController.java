package com.fooddecode.controller;

import com.fooddecode.dto.cart.AddCartItemRequest;
import com.fooddecode.dto.cart.CartAnalysisResponse;
import com.fooddecode.dto.cart.CartResponse;
import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> get() {
        return ApiResponse.ok("Cart", cartService.getCart());
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> add(@RequestBody AddCartItemRequest request) {
        return ApiResponse.ok("Added to cart", cartService.addItem(request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> quantity(@PathVariable Long id, @RequestBody AddCartItemRequest request) {
        int qty = request.getQuantity() == null ? 1 : request.getQuantity();
        return ApiResponse.ok("Cart updated", cartService.setQuantity(id, qty));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> remove(@PathVariable Long id) {
        return ApiResponse.ok("Removed", cartService.removeItem(id));
    }

    @GetMapping("/analysis")
    public ApiResponse<CartAnalysisResponse> analysis() {
        return ApiResponse.ok("Cart analysis", cartService.analysis());
    }
}
