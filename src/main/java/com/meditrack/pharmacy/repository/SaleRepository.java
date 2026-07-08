package com.meditrack.pharmacy.repository;

import com.meditrack.pharmacy.model.Sale;
import com.meditrack.pharmacy.model.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findAllByOrderByCreatedAtDesc();
    List<Sale> findTop5ByOrderByCreatedAtDesc();
    long countByStatus(SaleStatus status);
    List<Sale> findByStatus(SaleStatus status);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.medicine WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);
}
