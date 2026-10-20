package com.fooddecode.dto.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummarizeRequest {

    @NotNull
    private Long productId;
}
