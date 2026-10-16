package com.fooddecode.controller;

import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.product.ExplanationResponse;
import com.fooddecode.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final AiService aiService;

    @GetMapping("/{id}/explanation")
    public ApiResponse<ExplanationResponse> explanation(
            @PathVariable Long id, @RequestParam(defaultValue = "en") String lang) {
        return ApiResponse.ok("Explanation", aiService.explainIngredient(id, lang));
    }
}
