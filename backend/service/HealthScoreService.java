package com.fooddecode.service;

import com.fooddecode.entity.Ingredient;
import com.fooddecode.entity.NutritionFacts;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.ProductIngredient;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Health score used in the viva:
 * <pre>
 * start at 100
 * −8 per harmful ingredient (capped at −40)
 * extra −15 if any ingredient harm_category = trans_fat
 * extra −10 if any ingredient harm_category = artificial_sweetener
 * −20 if sugar_g &gt; 25, else −10 if sugar_g &gt; 15
 * −15 if sodium_mg &gt; 800, else −8 if sodium_mg &gt; 400
 * −5  if calories &gt; 300
 * +5  if fibre_g &gt;= 3
 * +5  if protein_g &gt;= 8
 * clamp to 0–100
 * </pre>
 */
@Service
public class HealthScoreService {

    public int calculate(Product product, List<ProductIngredient> links, NutritionFacts nutrition) {
        int score = 100;
        int harmfulDeduction = 0;
        boolean transFat = false;
        boolean artificialSweetener = false;

        for (ProductIngredient link : links) {
            Ingredient ingredient = link.getIngredient();
            if (ingredient.isHarmful()) {
                harmfulDeduction += 8;
            }
            String category = ingredient.getHarmCategory();
            if ("trans_fat".equalsIgnoreCase(category)) {
                transFat = true;
            }
            if ("artificial_sweetener".equalsIgnoreCase(category)) {
                artificialSweetener = true;
            }
        }
        score -= Math.min(40, harmfulDeduction);
        if (transFat) {
            score -= 15;
        }
        if (artificialSweetener) {
            score -= 10;
        }
        if (nutrition != null) {
            score -= sugarPenalty(nutrition.getSugarG());
            score -= sodiumPenalty(nutrition.getSodiumMg());
            if (gt(nutrition.getCalories(), 300)) {
                score -= 5;
            }
            if (gte(nutrition.getFibreG(), 3)) {
                score += 5;
            }
            if (gte(nutrition.getProteinG(), 8)) {
                score += 5;
            }
        }
        return Math.max(0, Math.min(100, score));
    }

    private int sugarPenalty(BigDecimal sugarG) {
        if (gt(sugarG, 25)) {
            return 20;
        }
        if (gt(sugarG, 15)) {
            return 10;
        }
        return 0;
    }

    private int sodiumPenalty(BigDecimal sodiumMg) {
        if (gt(sodiumMg, 800)) {
            return 15;
        }
        if (gt(sodiumMg, 400)) {
            return 8;
        }
        return 0;
    }

    private boolean gt(BigDecimal value, double threshold) {
        return value != null && value.compareTo(BigDecimal.valueOf(threshold)) > 0;
    }

    private boolean gte(BigDecimal value, double threshold) {
        return value != null && value.compareTo(BigDecimal.valueOf(threshold)) >= 0;
    }
}
