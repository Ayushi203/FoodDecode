package com.fooddecode.repository;

import com.fooddecode.entity.ScanHistory;
import com.fooddecode.entity.User;
import com.fooddecode.enums.ScanMethod;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

    Page<ScanHistory> findByUserOrderByScannedAtDesc(User user, Pageable pageable);

    Page<ScanHistory> findByUserAndScanMethodOrderByScannedAtDesc(User user, ScanMethod method, Pageable pageable);

    List<ScanHistory> findByUserOrderByScannedAtAsc(User user);

    long countByUser(User user);

    @Query("SELECT DISTINCT s.product.id FROM ScanHistory s WHERE s.user = :user")
    List<Long> findDistinctProductIdsByUser(@Param("user") User user);

    @Query("""
            SELECT s.product.id, s.product.name, COUNT(s)
            FROM ScanHistory s
            GROUP BY s.product.id, s.product.name
            ORDER BY COUNT(s) DESC
            """)
    List<Object[]> findMostScannedProducts(Pageable pageable);
}
