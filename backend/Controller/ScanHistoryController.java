package com.fooddecode.controller;

import com.fooddecode.dto.common.ApiResponse;
import com.fooddecode.dto.history.CreateScanHistoryRequest;
import com.fooddecode.dto.history.ScanHistoryResponse;
import com.fooddecode.service.ScanHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scan-history")
@RequiredArgsConstructor
public class ScanHistoryController {

    private final ScanHistoryService scanHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScanHistoryResponse> create(@Valid @RequestBody CreateScanHistoryRequest request) {
        return ApiResponse.ok("Scan logged", scanHistoryService.log(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        scanHistoryService.delete(id);
        return ApiResponse.ok("Scan removed");
    }
}
