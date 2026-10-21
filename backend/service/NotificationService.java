package com.fooddecode.service;

import com.fooddecode.dto.notification.MarkNotificationReadRequest;
import com.fooddecode.dto.notification.NotificationResponse;
import com.fooddecode.dto.recall.RecallAlertResponse;
import com.fooddecode.entity.NotificationRead;
import com.fooddecode.entity.NotificationReadId;
import com.fooddecode.entity.User;
import com.fooddecode.exception.ApiException;
import com.fooddecode.repository.NotificationReadRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    public static final String KIND_RECALL = "RECALL";

    private final CurrentUserService currentUserService;
    private final RecallService recallService;
    private final NotificationReadRepository notificationReadRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> list() {
        User user = currentUserService.requireUser();
        Set<Long> readIds = new HashSet<>();
        for (NotificationRead row : notificationReadRepository.findByUser(user)) {
            if (KIND_RECALL.equals(row.getId().getKind())) {
                readIds.add(row.getId().getRefId());
            }
        }
        return recallService.mine().stream()
                .map(alert -> toResponse(alert, !readIds.contains(alert.getProductId())))
                .toList();
    }

    @Transactional
    public void markRead(MarkNotificationReadRequest request) {
        if (request == null || request.getKind() == null || request.getRefId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "kind and refId are required");
        }
        User user = currentUserService.requireUser();
        NotificationReadId id = new NotificationReadId(user.getId(), request.getKind().trim(), request.getRefId());
        if (notificationReadRepository.existsById(id)) {
            return;
        }
        NotificationRead row = new NotificationRead();
        row.setId(id);
        row.setUser(user);
        row.setReadAt(Instant.now());
        notificationReadRepository.save(row);
    }

    private NotificationResponse toResponse(RecallAlertResponse alert, boolean unread) {
        String reason = alert.getRecallReason() == null || alert.getRecallReason().isBlank()
                ? "This pack is currently recalled."
                : alert.getRecallReason();
        return NotificationResponse.builder()
                .kind(KIND_RECALL)
                .refId(alert.getProductId())
                .title(alert.getName())
                .body(reason)
                .unread(unread)
                .href("/products/" + alert.getProductId())
                .build();
    }
}
