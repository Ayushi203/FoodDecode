package com.fooddecode.controller;

import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.history.ScanHistoryResponse;
import com.fooddecode.dto.product.ProductSummaryResponse;
import com.fooddecode.enums.ScanMethod;
import com.fooddecode.dto.notification.MarkNotificationReadRequest;
import com.fooddecode.dto.notification.NotificationResponse;
import com.fooddecode.service.FavoriteService;
import com.fooddecode.service.NotificationService;
import com.fooddecode.service.RecallService;
import com.fooddecode.service.ScanHistoryService;
import com.fooddecode.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserCollectionsController {

    private final ScanHistoryService scanHistoryService;
    private final FavoriteService favoriteService;
    private final RecallService recallService;
    private final UserDashboardService userDashboardService;

    private final NotificationService notificationService;

    @GetMapping("/history")
    public ApiResponse<Page<ScanHistoryResponse>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ScanMethod method) {
        return ApiResponse.ok("History", scanHistoryService.list(page, size, method));
    }

    @GetMapping("/favorites")
    public ApiResponse<java.util.List<ProductSummaryResponse>> favorites() {
        return ApiResponse.ok("Favorites", favoriteService.list());
    }

    @GetMapping("/dashboard")
    public ApiResponse<com.fooddecode.dto.user.UserDashboardResponse> dashboard() {
        return ApiResponse.ok("Dashboard", userDashboardService.load());
    }

    @GetMapping("/notifications")
    public ApiResponse<java.util.List<NotificationResponse>> notifications() {
        return ApiResponse.ok("Notifications", notificationService.list());
    }

    @PostMapping("/notifications/read")
    public ApiResponse<Void> markNotificationRead(@RequestBody MarkNotificationReadRequest request) {
        notificationService.markRead(request);
        return ApiResponse.ok("Notification marked read");
    }

    @GetMapping("/recall-alerts")
    public ApiResponse<java.util.List<com.fooddecode.dto.recall.RecallAlertResponse>> recallAlerts() {
        return ApiResponse.ok("Recall alerts", recallService.mine());
    }
}
