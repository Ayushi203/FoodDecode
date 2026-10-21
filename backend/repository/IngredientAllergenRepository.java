package com.fooddecode.repository;

import com.fooddecode.entity.IngredientAllergen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngredientAllergenRepository extends JpaRepository<IngredientAllergen, IngredientAllergen.Id> {

    @Query("""
            SELECT DISTINCT a.name
            FROM ProductIngredient pi
            JOIN pi.ingredient i
            JOIN IngredientAllergen ia ON ia.ingredient = i
            JOIN ia.allergen a
            WHERE pi.product.id = :productId
            """)
    List<String> findAllergenNamesByProductId(@Param("productId") Long productId);
}
