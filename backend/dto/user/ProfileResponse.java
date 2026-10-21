package com.fooddecode.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String role;
    private final String preferredLanguage;
    private final boolean verified;
    private final String dietPreference;
    private final java.util.List<Long> allergenIds;
    private final java.util.List<String> allergenNames;
}
