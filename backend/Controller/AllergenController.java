package com.fooddecode.controller;

import com.fooddecode.dto.allergen.AllergenResponse;
import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.service.AllergenService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/allergens")
@RequiredArgsConstructor
public class AllergenController {

    private final AllergenService allergenService;

    @GetMapping
    public ApiResponse<List<AllergenResponse>> list() {
        return ApiResponse.ok("Allergens", allergenService.listAll());
    }
}
