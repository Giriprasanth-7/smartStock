package com.stocksense.smartstock.service;

import com.stocksense.smartstock.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

        private final ProductRepository productRepository;
        private final ProductService productService;

        public RecommendationService(
                        ProductRepository productRepository,
                        ProductService productService) {

                this.productRepository = productRepository;
                this.productService = productService;
        }

        // =========================================================
        // SMART RECOMMENDATION
        // =========================================================

        public Map<String, Object> getRecommendation(Long productId) {

                // ---------------------------------------------------------
                // VERIFY PRODUCT EXISTS
                // ---------------------------------------------------------

                productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product not found with id: " + productId));

                // ---------------------------------------------------------
                // USE SAME SMART REPLENISHMENT ENGINE
                // ---------------------------------------------------------

                Map<String, Object> replenishment = productService.getSmartReplenishment(productId);

                // ---------------------------------------------------------
                // RESPONSE FOR FRONTEND
                // ---------------------------------------------------------

                Map<String, Object> recommendation = new HashMap<>();

                recommendation.put(
                                "productId",
                                replenishment.get("productId"));

                recommendation.put(
                                "productName",
                                replenishment.get("productName"));

                recommendation.put(
                                "sku",
                                replenishment.get("sku"));

                recommendation.put(
                                "currentStock",
                                replenishment.get("currentStock"));

                recommendation.put(
                                "reorderLevel",
                                replenishment.get("reorderLevel"));

                recommendation.put(
                                "averageDailyDemand",
                                replenishment.get("averageDailyDemand"));

                recommendation.put(
                                "leadTimeDays",
                                replenishment.get("leadTimeDays"));

                recommendation.put(
                                "leadTimeDemand",
                                replenishment.get("leadTimeDemand"));

                recommendation.put(
                                "safetyStock",
                                replenishment.get("safetyStock"));

                // ---------------------------------------------------------
                // EFFECTIVE REORDER POINT
                // ---------------------------------------------------------
                //
                // This is calculated by ProductService using:
                //
                // max(
                // configured reorder level,
                // lead time demand + safety stock
                // )
                //
                // ---------------------------------------------------------

                recommendation.put(
                                "effectiveReorderPoint",
                                replenishment.get("effectiveReorderPoint"));

                recommendation.put(
                                "demandBasedReorderPoint",
                                replenishment.get("demandBasedReorderPoint"));

                recommendation.put(
                                "minimumOrderQuantity",
                                replenishment.get("minimumOrderQuantity"));

                recommendation.put(
                                "maximumStockLevel",
                                replenishment.get("maximumStockLevel"));

                recommendation.put(
                                "targetStock",
                                replenishment.get("targetStock"));

                recommendation.put(
                                "recommendedOrderQuantity",
                                replenishment.get("recommendedOrderQuantity"));

                recommendation.put(
                                "status",
                                replenishment.get("status"));

                return recommendation;
        }
}