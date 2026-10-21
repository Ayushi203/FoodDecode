package com.fooddecode.service;

import com.fooddecode.dto.admin.RecallRequest;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.dto.recall.RecallAlertResponse;
import com.fooddecode.entity.Cart;
import com.fooddecode.entity.Favorite;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.User;
import com.fooddecode.exception.ApiException;
import com.fooddecode.mapper.ProductMapper;
import com.fooddecode.repository.CartItemRepository;
import com.fooddecode.repository.CartRepository;
import com.fooddecode.repository.FavoriteRepository;
import com.fooddecode.repository.ProductRepository;
import com.fooddecode.repository.ScanHistoryRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecallService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final FavoriteRepository favoriteRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public ProductSummaryResponse flag(Long productId, RecallRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        String reason = request == null || request.getReason() == null || request.getReason().isBlank()
                ? "Recalled by administrator"
                : request.getReason().trim();
        product.setRecalled(true);
        product.setRecallReason(reason);
        return ProductMapper.toSummary(productRepository.save(product));
    }

    @Transactional
    public ProductSummaryResponse clear(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setRecalled(false);
        product.setRecallReason(null);
        return ProductMapper.toSummary(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<RecallAlertResponse> mine() {
        User user = currentUserService.requireUser();
        Set<Long> favIds = new HashSet<>();
        for (Favorite fav : favoriteRepository.findByUserOrderByAddedAtDesc(user)) {
            favIds.add(fav.getProduct().getId());
        }
        Set<Long> histIds = new HashSet<>(scanHistoryRepository.findDistinctProductIdsByUser(user));
        Set<Long> cartIds = new HashSet<>();
        cartRepository.findByUser(user).ifPresent(cart -> {
            for (var item : cartItemRepository.findByCartOrderByIdAsc(cart)) {
                cartIds.add(item.getProduct().getId());
            }
        });
        Set<Long> watched = new HashSet<>();
        watched.addAll(favIds);
        watched.addAll(histIds);
        watched.addAll(cartIds);
        List<RecallAlertResponse> alerts = new ArrayList<>();
        for (Product product : productRepository.findByRecalledTrue()) {
            if (!watched.contains(product.getId())) {
                continue;
            }
            alerts.add(RecallAlertResponse.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .brand(product.getBrand())
                    .recallReason(product.getRecallReason())
                    .inFavorites(favIds.contains(product.getId()))
                    .inCart(cartIds.contains(product.getId()))
                    .inHistory(histIds.contains(product.getId()))
                    .build());
        }
        return alerts;
    }
}
