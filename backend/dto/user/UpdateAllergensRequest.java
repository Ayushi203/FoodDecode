package com.fooddecode.dto.user;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAllergensRequest {
    private List<Long> allergenIds;
}
