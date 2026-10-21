package com.fooddecode.repository;

import com.fooddecode.entity.IngredientExplanation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientExplanationRepository extends JpaRepository<IngredientExplanation, Long> {

    Optional<IngredientExplanation> findByIngredientIdAndLanguageCode(Long ingredientId, String languageCode);
}
