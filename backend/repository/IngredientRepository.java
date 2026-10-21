package com.fooddecode.repository;

import com.fooddecode.entity.Ingredient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNameIgnoreCase(String name);

    List<Ingredient> findByHarmfulTrue();
}
