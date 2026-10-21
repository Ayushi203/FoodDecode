package com.fooddecode.dto.history;

import com.fooddecode.enums.ScanMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateScanHistoryRequest {

    @NotNull
    private Long productId;

    @NotNull
    private ScanMethod scanMethod;
}
