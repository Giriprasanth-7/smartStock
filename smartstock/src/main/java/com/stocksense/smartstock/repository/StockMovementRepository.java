package com.stocksense.smartstock.repository;

import com.stocksense.smartstock.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository
                extends JpaRepository<StockMovement, Long> {

        // =========================================================
        // GET MOVEMENTS BY PRODUCT
        // =========================================================

        List<StockMovement> findByProductId(Long productId);

        // =========================================================
        // TOTAL SALES BY PRODUCT
        // =========================================================

        @Query("""
                        SELECT SUM(s.quantity)
                        FROM StockMovement s
                        WHERE s.productId = :productId
                        AND UPPER(s.movementType) = 'SALE'
                        """)
        Integer getTotalSalesByProductId(
                        @Param("productId") Long productId);

        // =========================================================
        // SALES SINCE DATE
        // =========================================================

        @Query("""
                        SELECT SUM(s.quantity)
                        FROM StockMovement s
                        WHERE s.productId = :productId
                        AND UPPER(s.movementType) = 'SALE'
                        AND s.movementDate >= :startDate
                        """)
        Integer getSalesSince(
                        @Param("productId") Long productId,
                        @Param("startDate") LocalDateTime startDate);

        // =========================================================
        // SALES BETWEEN TWO DATES
        // =========================================================

        @Query("""
                        SELECT SUM(s.quantity)
                        FROM StockMovement s
                        WHERE s.productId = :productId
                        AND UPPER(s.movementType) = 'SALE'
                        AND s.movementDate >= :startDate
                        AND s.movementDate < :endDate
                        """)
        Integer getSalesBetween(
                        @Param("productId") Long productId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}