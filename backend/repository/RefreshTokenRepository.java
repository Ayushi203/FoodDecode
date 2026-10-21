package com.fooddecode.repository;

import com.fooddecode.entity.RefreshToken;
import com.fooddecode.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    void deleteByUser(User user);
}
