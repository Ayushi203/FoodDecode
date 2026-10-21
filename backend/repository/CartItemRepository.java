package com.fooddecode.repository;

import com.fooddecode.entity.Cart;
import com.fooddecode.entity.CartItem;
import com.fooddecode.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartOrderByIdAsc(Cart cart);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
