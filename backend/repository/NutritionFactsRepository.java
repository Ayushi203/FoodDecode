package com.fooddecode.repository;

import com.fooddecode.entity.NutritionFacts;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionFactsRepository extends JpaRepository<NutritionFacts, Long> {

    Optional<NutritionFacts> findByProductId(Long productId);
}
