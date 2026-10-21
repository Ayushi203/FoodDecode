package com.fooddecode.repository;

import com.fooddecode.entity.AiResponseCache;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiResponseCacheRepository extends JpaRepository<AiResponseCache, Long> {

    Optional<AiResponseCache> findByCacheKeyAndExpiresAtAfter(String cacheKey, Instant now);
}
