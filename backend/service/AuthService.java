package com.fooddecode.service;

import com.fooddecode.dto.auth.AuthTokenResponse;
import com.fooddecode.dto.auth.ForgotPasswordRequest;
import com.fooddecode.dto.auth.LoginRequest;
import com.fooddecode.dto.auth.RefreshTokenRequest;
import com.fooddecode.dto.auth.RegisterRequest;
import com.fooddecode.dto.auth.ResetPasswordRequest;
import com.fooddecode.dto.auth.ResetTokenResponse;
import com.fooddecode.dto.auth.VerifyOtpRequest;
import com.fooddecode.entity.OtpVerification;
import com.fooddecode.entity.RefreshToken;
import com.fooddecode.entity.User;
import com.fooddecode.enums.OtpPurpose;
import com.fooddecode.enums.UserRole;
import com.fooddecode.exception.ApiException;
import com.fooddecode.repository.OtpVerificationRepository;
import com.fooddecode.repository.RefreshTokenRepository;
import com.fooddecode.repository.UserRepository;
import com.fooddecode.security.JwtService;
import com.fooddecode.security.TokenHasher;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setVerified(true);
        user.setPreferredLanguage("en");
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return issueTokens(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No account for that email"));

        Instant windowStart = Instant.now().minus(15, ChronoUnit.MINUTES);
        long recent = otpVerificationRepository.countByUserAndPurposeAndCreatedAtAfter(
                user, OtpPurpose.PASSWORD_RESET, windowStart);
        if (recent >= 3) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many OTP requests. Try again in 15 minutes.");
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        OtpVerification row = new OtpVerification();
        row.setUser(user);
        row.setOtpCode(passwordEncoder.encode(otp));
        row.setPurpose(OtpPurpose.PASSWORD_RESET);
        row.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        row.setUsed(false);
        otpVerificationRepository.save(row);
        emailService.sendOtp(user.getEmail(), otp);
    }

    @Transactional
    public ResetTokenResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP"));
        OtpVerification match = otpVerificationRepository
                .findByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, OtpPurpose.PASSWORD_RESET)
                .stream()
                .filter(row -> Instant.now().isBefore(row.getExpiresAt()))
                .filter(row -> passwordEncoder.matches(request.getOtp(), row.getOtpCode()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP"));
        match.setUsed(true);
        return new ResetTokenResponse(jwtService.createPasswordResetToken(user.getEmail()));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!jwtService.isValid(request.getResetToken(), "reset")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token is invalid or expired");
        }
        String email = jwtService.extractEmail(request.getResetToken());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        String raw = request.getRefreshToken();
        if (!jwtService.isValid(raw, "refresh")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid");
        }
        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(TokenHasher.sha256(raw))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is revoked"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        stored.setRevoked(true);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHashAndRevokedFalse(TokenHasher.sha256(request.getRefreshToken()))
                .ifPresent(token -> token.setRevoked(true));
    }

    private AuthTokenResponse issueTokens(User user) {
        String access = jwtService.createAccessToken(user.getEmail(), user.getRole().name());
        String refresh = jwtService.createRefreshToken(user.getEmail());
        RefreshToken row = new RefreshToken();
        row.setUser(user);
        row.setTokenHash(TokenHasher.sha256(refresh));
        row.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()));
        row.setRevoked(false);
        refreshTokenRepository.save(row);
        return new AuthTokenResponse(
                access, refresh, "Bearer", user.getRole().name(), user.getFullName(), user.getEmail());
    }
}
