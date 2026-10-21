package com.fooddecode.dto.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkNotificationReadRequest {
    private String kind;
    private Long refId;
}
