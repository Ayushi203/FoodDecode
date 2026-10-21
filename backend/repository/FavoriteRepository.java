package com.fooddecode.repository;

import com.fooddecode.entity.Favorite;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserOrderByAddedAtDesc(User user);

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct_Id(User user, Long productId);
}
