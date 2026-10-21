package com.fooddecode.repository;

import com.fooddecode.entity.User;
import com.fooddecode.entity.UserAllergen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAllergenRepository extends JpaRepository<UserAllergen, UserAllergen.Id> {

    List<UserAllergen> findByUser(User user);

    void deleteByUser(User user);
}
