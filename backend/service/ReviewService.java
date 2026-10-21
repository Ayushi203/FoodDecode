package com.fooddecode.service;

import com.fooddecode.dto.review.ReviewRequest;
import com.fooddecode.dto.review.ReviewResponse;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.Review;
import com.fooddecode.entity.User;
import com.fooddecode.enums.UserRole;
import com.fooddecode.exception.ApiException;
import com.fooddecode.repository.ProductRepository;
import com.fooddecode.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ReviewResponse> list(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        return reviewRepository.findByProductOrderByCreatedAtDesc(product).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReviewResponse upsert(Long productId, ReviewRequest request) {
        User user = currentUserService.requireUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        Review review = reviewRepository.findByUserAndProduct(user, product).orElseGet(() -> {
            Review created = new Review();
            created.setUser(user);
            created.setProduct(product);
            return created;
        });
        review.setRating(request.getRating());
        review.setComment(request.getComment() == null ? null : request.getComment().trim());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(Long reviewId) {
        User user = currentUserService.requireUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        boolean owner = review.getUser().getId().equals(user.getId());
        boolean admin = user.getRole() == UserRole.ADMIN;
        if (!owner && !admin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own review");
        }
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
