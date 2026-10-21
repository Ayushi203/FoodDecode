package com.fooddecode.repository;

import com.fooddecode.entity.ProductIngredient;
import com.fooddecode.entity.ProductIngredientId;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, ProductIngredientId> {

    List<ProductIngredient> findByProductIdOrderByPositionInListAsc(Long productId);

    boolean existsByIngredientId(Long ingredientId);

    @Query("""
            SELECT i.id, i.name, i.harmCategory, COUNT(pi)
            FROM ProductIngredient pi
            JOIN pi.ingredient i
            WHERE i.harmful = true
            GROUP BY i.id, i.name, i.harmCategory
            ORDER BY COUNT(pi) DESC
            """)
    List<Object[]> findMostFlaggedHarmfulIngredients(Pageable pageable);
}
