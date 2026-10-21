package com.fooddecode.repository;

import com.fooddecode.entity.Cart;
import com.fooddecode.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
