package com.fooddecode.controller;

import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.user.ChangePasswordRequest;
import com.fooddecode.dto.user.ProfileResponse;
import com.fooddecode.dto.user.UpdateAllergensRequest;
import com.fooddecode.dto.user.UpdateDietRequest;
import com.fooddecode.dto.user.UpdateProfileRequest;
import com.fooddecode.dto.allergen.AllergenResponse;
import com.fooddecode.service.AllergenService;
import com.fooddecode.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AllergenService allergenService;

    @GetMapping
    public ApiResponse<ProfileResponse> me() {
        return ApiResponse.ok("Profile", profileService.me());
    }

    @PutMapping
    public ApiResponse<ProfileResponse> update(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", profileService.update(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> password(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ApiResponse.ok("Password changed");
    }

    @PutMapping("/allergens")
    public ApiResponse<java.util.List<AllergenResponse>> allergens(@Valid @RequestBody UpdateAllergensRequest request) {
        return ApiResponse.ok("Allergens saved", allergenService.replaceMine(request));
    }

    @PutMapping("/diet-preference")
    public ApiResponse<ProfileResponse> diet(@Valid @RequestBody UpdateDietRequest request) {
        return ApiResponse.ok("Diet preference saved", profileService.updateDiet(request));
    }
}
