package com.fooddecode.repository;

import com.fooddecode.entity.Product;
import com.fooddecode.entity.Review;
import com.fooddecode.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    Optional<Review> findByUserAndProduct(User user, Product product);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double averageRating(@Param("productId") Long productId);

    long countByProductId(Long productId);
}
