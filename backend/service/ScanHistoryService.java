package com.fooddecode.service;

import com.fooddecode.dto.history.CreateScanHistoryRequest;
import com.fooddecode.dto.history.ScanHistoryResponse;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.ScanHistory;
import com.fooddecode.entity.User;
import com.fooddecode.enums.ScanMethod;
import com.fooddecode.exception.ApiException;
import com.fooddecode.mapper.ProductMapper;
import com.fooddecode.repository.ScanHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScanHistoryService {

    private final ScanHistoryRepository scanHistoryRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<ScanHistoryResponse> list(int page, int size, ScanMethod method) {
        User user = currentUserService.requireUser();
        PageRequest pageable = PageRequest.of(page, size);
        Page<ScanHistory> rows = method == null
                ? scanHistoryRepository.findByUserOrderByScannedAtDesc(user, pageable)
                : scanHistoryRepository.findByUserAndScanMethodOrderByScannedAtDesc(user, method, pageable);
        return rows.map(this::toResponse);
    }

    @Transactional
    public ScanHistoryResponse log(CreateScanHistoryRequest request) {
        User user = currentUserService.requireUser();
        Product product = productService.requireProduct(request.getProductId());
        ScanHistory row = new ScanHistory();
        row.setUser(user);
        row.setProduct(product);
        row.setScanMethod(request.getScanMethod());
        return toResponse(scanHistoryRepository.save(row));
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserService.requireUser();
        ScanHistory row = scanHistoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "History item not found"));
        if (!row.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot delete another user's history");
        }
        scanHistoryRepository.delete(row);
    }

    private ScanHistoryResponse toResponse(ScanHistory row) {
        return ScanHistoryResponse.builder()
                .id(row.getId())
                .product(ProductMapper.toSummary(row.getProduct()))
                .scanMethod(row.getScanMethod().name())
                .scannedAt(row.getScannedAt())
                .build();
    }
}
