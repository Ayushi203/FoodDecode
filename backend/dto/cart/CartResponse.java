package com.fooddecode.dto.cart;

import com.fooddecode.dto.product.ProductSummaryResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartResponse {
    private final Long cartId;
    private final List<CartItemResponse> items;

    @Getter
    @Builder
    public static class CartItemResponse {
        private final Long id;
        private final int quantity;
        private final ProductSummaryResponse product;
    }
}
