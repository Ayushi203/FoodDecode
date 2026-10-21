package com.fooddecode.dto.user;

import com.fooddecode.enums.DietPreference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDietRequest {
    private DietPreference dietPreference;
}
