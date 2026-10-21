package com.fooddecode.service;

import com.fooddecode.dto.user.ChangePasswordRequest;
import com.fooddecode.dto.user.ProfileResponse;
import com.fooddecode.dto.user.UpdateDietRequest;
import com.fooddecode.dto.user.UpdateProfileRequest;
import com.fooddecode.entity.User;
import com.fooddecode.entity.UserAllergen;
import com.fooddecode.enums.DietPreference;
import com.fooddecode.exception.ApiException;
import com.fooddecode.repository.RefreshTokenRepository;
import com.fooddecode.repository.UserAllergenRepository;
import com.fooddecode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAllergenRepository userAllergenRepository;

    @Transactional(readOnly = true)
    public ProfileResponse me() {
        return toResponse(currentUserService.requireUser());
    }

    @Transactional
    public ProfileResponse update(UpdateProfileRequest request) {
        User user = currentUserService.requireUser();
        user.setFullName(request.getFullName().trim());
        if (request.getPreferredLanguage() != null && !request.getPreferredLanguage().isBlank()) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.requireUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public ProfileResponse updateDiet(UpdateDietRequest request) {
        if (request.getDietPreference() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dietPreference is required");
        }
        User user = currentUserService.requireUser();
        user.setDietPreference(request.getDietPreference());
        return toResponse(userRepository.save(user));
    }

    private ProfileResponse toResponse(User user) {
        java.util.List<UserAllergen> links = userAllergenRepository.findByUser(user);
        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .preferredLanguage(user.getPreferredLanguage())
                .verified(user.isVerified())
                .dietPreference((user.getDietPreference() == null ? DietPreference.NONE : user.getDietPreference()).name())
                .allergenIds(links.stream().map(ua -> ua.getAllergen().getId()).toList())
                .allergenNames(links.stream().map(ua -> ua.getAllergen().getName()).toList())
                .build();
    }
}
