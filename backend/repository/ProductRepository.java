package com.fooddecode.repository;

import com.fooddecode.entity.Product;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByRecalledTrue();

    Optional<Product> findByBarcode(String barcode);

    List<Product> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String name, String brand);

    @Query("select distinct p.category from Product p where p.category is not null order by p.category")
    List<String> findDistinctCategories();

    List<Product> findByCategoryIgnoreCaseAndHealthScoreGreaterThanAndIdNotOrderByHealthScoreDesc(
            String category, Integer healthScore, Long id, Pageable pageable);

    @Query(
            "select p from Product p where lower(p.category) in :categories and p.healthScore > :score "
                    + "and p.id <> :id order by p.healthScore desc")
    List<Product> findByCategoryIgnoreCaseInAndHealthScoreGreaterThanAndIdNotOrderByHealthScoreDesc(
            @Param("categories") Collection<String> categories,
            @Param("score") Integer healthScore,
            @Param("id") Long id,
            Pageable pageable);

    @Query("select p from Product p where lower(p.category) in :categories and p.id <> :id order by p.healthScore desc")
    List<Product> findByCategoryIgnoreCaseInAndIdNotOrderByHealthScoreDesc(
            @Param("categories") Collection<String> categories, @Param("id") Long id, Pageable pageable);

    List<Product> findByHealthScoreGreaterThanAndIdNotOrderByHealthScoreDesc(
            Integer healthScore, Long id, Pageable pageable);

    List<Product> findByIdNotOrderByHealthScoreDesc(Long id, Pageable pageable);
}
