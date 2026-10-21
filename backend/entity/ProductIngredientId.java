package com.fooddecode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredientId implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductIngredientId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId)
                && Objects.equals(ingredientId, that.ingredientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, ingredientId);
    }

    @Override
    public String toString() {
        return "ProductIngredientId[productId=" + productId + ", ingredientId=" + ingredientId + "]";
    }
}
