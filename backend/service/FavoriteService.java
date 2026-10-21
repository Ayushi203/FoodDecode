package com.fooddecode.service;

import com.fooddecode.dto.favorite.CreateFavoriteRequest;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.entity.Favorite;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.User;
import com.fooddecode.exception.ApiException;
import com.fooddecode.mapper.ProductMapper;
import com.fooddecode.repository.FavoriteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> list() {
        User user = currentUserService.requireUser();
        return favoriteRepository.findByUserOrderByAddedAtDesc(user).stream()
                .map(Favorite::getProduct)
                .map(ProductMapper::toSummary)
                .toList();
    }

    @Transactional
    public ProductSummaryResponse add(CreateFavoriteRequest request) {
        User user = currentUserService.requireUser();
        Product product = productService.requireProduct(request.getProductId());
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new ApiException(HttpStatus.CONFLICT, "Already in favorites");
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
        return ProductMapper.toSummary(product);
    }

    @Transactional
    public void remove(Long productId) {
        User user = currentUserService.requireUser();
        favoriteRepository.deleteByUserAndProduct_Id(user, productId);
    }
}
