package com.fooddecode.dto.history;

import com.fooddecode.dto.product.ProductSummaryResponse;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScanHistoryResponse {

    private final Long id;
    private final ProductSummaryResponse product;
    private final String scanMethod;
    private final Instant scannedAt;
}
