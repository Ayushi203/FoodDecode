package com.fooddecode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "ingredient_explanations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ingredient_lang",
                columnNames = {"ingredient_id", "language_code"}))
@Getter
@Setter
@NoArgsConstructor
public class IngredientExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode = "en";

    @Column(name = "simple_explanation", nullable = false, columnDefinition = "TEXT")
    private String simpleExplanation;

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
