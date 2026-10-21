package com.fooddecode.dto.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final List<String> errors;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, List.of());
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, List.of());
    }

    public static ApiResponse<Void> fail(String message, List<String> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
