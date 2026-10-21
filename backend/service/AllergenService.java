package com.fooddecode.service;

import com.fooddecode.dto.allergen.AllergenResponse;
import com.fooddecode.dto.user.UpdateAllergensRequest;
import com.fooddecode.entity.Allergen;
import com.fooddecode.entity.User;
import com.fooddecode.entity.UserAllergen;
import com.fooddecode.exception.ApiException;
import com.fooddecode.repository.AllergenRepository;
import com.fooddecode.repository.IngredientAllergenRepository;
import com.fooddecode.repository.UserAllergenRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AllergenService {

    private final AllergenRepository allergenRepository;
    private final UserAllergenRepository userAllergenRepository;
    private final IngredientAllergenRepository ingredientAllergenRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<AllergenResponse> listAll() {
        return allergenRepository.findAll().stream()
                .map(a -> AllergenResponse.builder().id(a.getId()).name(a.getName()).build())
                .toList();
    }

    @Transactional
    public List<AllergenResponse> replaceMine(UpdateAllergensRequest request) {
        User user = currentUserService.requireUser();
        userAllergenRepository.deleteByUser(user);
        userAllergenRepository.flush();
        List<Long> ids = request.getAllergenIds() == null ? List.of() : request.getAllergenIds();
        for (Long allergenId : new HashSet<>(ids)) {
            Allergen allergen = allergenRepository.findById(allergenId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Unknown allergen " + allergenId));
            UserAllergen link = new UserAllergen();
            UserAllergen.Id key = new UserAllergen.Id();
            key.setUserId(user.getId());
            key.setAllergenId(allergen.getId());
            link.setId(key);
            link.setUser(user);
            link.setAllergen(allergen);
            userAllergenRepository.save(link);
        }
        return userAllergenRepository.findByUser(user).stream()
                .map(ua -> AllergenResponse.builder().id(ua.getAllergen().getId()).name(ua.getAllergen().getName()).build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> warningsFor(Long productId, User user) {
        if (user == null) {
            return List.of();
        }
        Set<String> selected = new HashSet<>();
        for (UserAllergen ua : userAllergenRepository.findByUser(user)) {
            selected.add(ua.getAllergen().getName().toLowerCase(Locale.ROOT));
        }
        if (selected.isEmpty()) {
            return List.of();
        }
        List<String> present = ingredientAllergenRepository.findAllergenNamesByProductId(productId);
        List<String> hits = new ArrayList<>();
        for (String name : present) {
            if (selected.contains(name.toLowerCase(Locale.ROOT))) {
                hits.add(name);
            }
        }
        return hits;
    }
}
