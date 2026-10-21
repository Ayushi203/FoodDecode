package com.fooddecode.repository;

import com.fooddecode.entity.NotificationRead;
import com.fooddecode.entity.NotificationReadId;
import com.fooddecode.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, NotificationReadId> {

    List<NotificationRead> findByUser(User user);
}
