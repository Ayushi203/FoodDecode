package com.fooddecode.repository;

import com.fooddecode.entity.Allergen;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    Optional<Allergen> findByNameIgnoreCase(String name);
}
