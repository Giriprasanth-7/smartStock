package com.stocksense.smartstock.controller;

import com.stocksense.smartstock.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {

        this.recommendationService = recommendationService;
    }

    // =========================================================
    // GET SMART RECOMMENDATION FOR A PRODUCT
    // =========================================================

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getRecommendation(
            @PathVariable Long productId) {

        try {

            Map<String, Object> recommendation = recommendationService.getRecommendation(productId);

            return ResponseEntity.ok(recommendation);

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .status(404)
                    .body(Map.of(
                            "error",
                            exception.getMessage()));
        }
    }

    // =========================================================
    // GET INVENTORY INTELLIGENCE SUMMARY
    // =========================================================

    @GetMapping("/intelligence/summary")
    public ResponseEntity<Map<String, Object>> getInventoryIntelligenceSummary() {

        return ResponseEntity.ok(
                recommendationService.getInventoryIntelligenceSummary());
    }
}