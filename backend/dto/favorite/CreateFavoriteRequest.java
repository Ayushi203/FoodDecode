package com.fooddecode.dto.favorite;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFavoriteRequest {

    @NotNull
    private Long productId;
}
