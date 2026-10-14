package com.fooddecode.controller;

import com.fooddecode.dto.auth.AuthTokenResponse;
import com.fooddecode.dto.auth.ForgotPasswordRequest;
import com.fooddecode.dto.auth.LoginRequest;
import com.fooddecode.dto.auth.RefreshTokenRequest;
import com.fooddecode.dto.auth.RegisterRequest;
import com.fooddecode.dto.auth.ResetPasswordRequest;
import com.fooddecode.dto.auth.ResetTokenResponse;
import com.fooddecode.dto.auth.VerifyOtpRequest;
import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Registered", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Logged in", authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok("If the account exists, an OTP was sent (or logged in the server console).");
    }

    @PostMapping("/verify-otp")
    public ApiResponse<ResetTokenResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ApiResponse.ok("OTP verified", authService.verifyOtp(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password updated");
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ApiResponse.ok("Logged out");
    }
}
