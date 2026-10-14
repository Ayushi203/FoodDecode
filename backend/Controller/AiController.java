package com.fooddecode.controller;

import com.fooddecode.dto.ai.AiTextResponse;
import com.fooddecode.dto.ai.SummarizeRequest;
import com.fooddecode.dto.ai.TranslateRequest;
import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.entity.Product;
import com.fooddecode.service.AiService;
import com.fooddecode.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ProductService productService;

    @PostMapping("/translate")
    public ApiResponse<AiTextResponse> translate(@Valid @RequestBody TranslateRequest request) {
        String text = aiService.translateText(request.getText(), request.getTargetLanguage());
        return ApiResponse.ok("Translation", new AiTextResponse(text));
    }

    @PostMapping("/summarize")
    public ApiResponse<AiTextResponse> summarize(@Valid @RequestBody SummarizeRequest request) {
        Product product = productService.requireProduct(request.getProductId());
        String text = aiService.summarizeProduct(product);
        return ApiResponse.ok("Summary", new AiTextResponse(text));
    }
}
