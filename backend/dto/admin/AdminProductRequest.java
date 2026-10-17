package com.fooddecode.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProductRequest {

    private String barcode;

    @NotBlank
    private String name;

    private String brand;
    private String category;
    private String imageUrl;
    private String purchaseUrl;
}
