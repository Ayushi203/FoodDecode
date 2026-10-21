package com.fooddecode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nutrition_facts")
@Getter
@Setter
@NoArgsConstructor
public class NutritionFacts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    private BigDecimal calories;

    @Column(name = "protein_g")
    private BigDecimal proteinG;

    @Column(name = "carbs_g")
    private BigDecimal carbsG;

    @Column(name = "fats_g")
    private BigDecimal fatsG;

    @Column(name = "sugar_g")
    private BigDecimal sugarG;

    @Column(name = "sodium_mg")
    private BigDecimal sodiumMg;

    @Column(name = "fibre_g")
    private BigDecimal fibreG;

    @Column(name = "serving_size", length = 50)
    private String servingSize;
}
