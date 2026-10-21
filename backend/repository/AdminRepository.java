package com.fooddecode.repository;

import com.fooddecode.entity.Admin;
import com.fooddecode.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUser(User user);
}
