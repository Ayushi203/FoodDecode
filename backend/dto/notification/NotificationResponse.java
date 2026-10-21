package com.fooddecode.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private final String kind;
    private final Long refId;
    private final String title;
    private final String body;
    private final boolean unread;
    private final String href;
}
