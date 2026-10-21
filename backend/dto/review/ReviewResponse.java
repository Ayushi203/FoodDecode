package com.fooddecode.dto.review;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {
    private final Long id;
    private final Long productId;
    private final Long userId;
    private final String userName;
    private final int rating;
    private final String comment;
    private final Instant createdAt;
}
