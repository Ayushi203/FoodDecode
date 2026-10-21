package com.fooddecode.repository;

import com.fooddecode.entity.OtpVerification;
import com.fooddecode.entity.User;
import com.fooddecode.enums.OtpPurpose;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    long countByUserAndPurposeAndCreatedAtAfter(User user, OtpPurpose purpose, Instant createdAfter);

    java.util.List<OtpVerification> findByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            User user, OtpPurpose purpose);
}
