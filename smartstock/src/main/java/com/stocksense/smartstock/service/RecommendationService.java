package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        // SMART RECOMMENDATION FOR ONE PRODUCT
        // =========================================================

        public Map<String, Object> getRecommendation(Long productId) {

                productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product not found with id: " + productId));

                Map<String, Object> replenishment = productService.getSmartReplenishment(productId);

                Map<String, Object> recommendation = new HashMap<>();

                recommendation.put("productId", replenishment.get("productId"));
                recommendation.put("productName", replenishment.get("productName"));
                recommendation.put("sku", replenishment.get("sku"));
                recommendation.put("currentStock", replenishment.get("currentStock"));
                recommendation.put("reorderLevel", replenishment.get("reorderLevel"));
                recommendation.put("averageDailyDemand", replenishment.get("averageDailyDemand"));
                recommendation.put("leadTimeDays", replenishment.get("leadTimeDays"));
                recommendation.put("leadTimeDemand", replenishment.get("leadTimeDemand"));
                recommendation.put("safetyStock", replenishment.get("safetyStock"));
                recommendation.put("effectiveReorderPoint", replenishment.get("effectiveReorderPoint"));
                recommendation.put("demandBasedReorderPoint", replenishment.get("demandBasedReorderPoint"));
                recommendation.put("minimumOrderQuantity", replenishment.get("minimumOrderQuantity"));
                recommendation.put("maximumStockLevel", replenishment.get("maximumStockLevel"));
                recommendation.put("targetStock", replenishment.get("targetStock"));
                recommendation.put("recommendedOrderQuantity",
                                replenishment.get("recommendedOrderQuantity"));
                recommendation.put("status", replenishment.get("status"));

                return recommendation;
        }

        // =========================================================
        // PHASE 5 - INVENTORY INTELLIGENCE
        // =========================================================

        public List<Map<String, Object>> getInventoryIntelligence() {

                List<Product> products = productRepository.findAll();

                List<Map<String, Object>> insights = new ArrayList<>();

                for (Product product : products) {

                        Map<String, Object> replenishment = productService.getSmartReplenishment(product.getId());

                        double currentStock = getDouble(replenishment.get("currentStock"));

                        double averageDailyDemand = getDouble(replenishment.get("averageDailyDemand"));

                        double effectiveReorderPoint = getDouble(replenishment.get("effectiveReorderPoint"));

                        double maximumStockLevel = getDouble(replenishment.get("maximumStockLevel"));

                        double daysOfStockRemaining = 0;

                        if (averageDailyDemand > 0) {
                                daysOfStockRemaining = currentStock / averageDailyDemand;
                        }

                        String intelligenceStatus;
                        int priorityScore;

                        if (currentStock <= 0) {

                                intelligenceStatus = "STOCKOUT_RISK";
                                priorityScore = 100;

                        } else if (currentStock <= effectiveReorderPoint) {

                                intelligenceStatus = "REORDER_REQUIRED";
                                priorityScore = 90;

                        } else if (maximumStockLevel > 0
                                        && currentStock >= maximumStockLevel) {

                                intelligenceStatus = "OVERSTOCKED";
                                priorityScore = 70;

                        } else if (averageDailyDemand > 0
                                        && daysOfStockRemaining >= 30) {

                                intelligenceStatus = "SLOW_MOVING";
                                priorityScore = 50;

                        } else {

                                intelligenceStatus = "HEALTHY";
                                priorityScore = 10;
                        }

                        Map<String, Object> insight = new HashMap<>();

                        insight.put("productId", product.getId());
                        insight.put("productName", product.getName());
                        insight.put("sku", product.getSku());
                        insight.put("currentStock", currentStock);
                        insight.put("averageDailyDemand", averageDailyDemand);
                        insight.put("effectiveReorderPoint", effectiveReorderPoint);
                        insight.put("maximumStockLevel", maximumStockLevel);

                        insight.put(
                                        "daysOfStockRemaining",
                                        Math.round(daysOfStockRemaining * 100.0) / 100.0);

                        insight.put(
                                        "recommendedOrderQuantity",
                                        replenishment.get("recommendedOrderQuantity"));

                        insight.put(
                                        "intelligenceStatus",
                                        intelligenceStatus);

                        insight.put(
                                        "priorityScore",
                                        priorityScore);

                        insights.add(insight);
                }

                insights.sort((first, second) -> Integer.compare(
                                (Integer) second.get("priorityScore"),
                                (Integer) first.get("priorityScore")));

                return insights;
        }

        // =========================================================
        // PHASE 5 - INVENTORY INTELLIGENCE SUMMARY
        // =========================================================

        public Map<String, Object> getInventoryIntelligenceSummary() {

                List<Map<String, Object>> insights = getInventoryIntelligence();

                int stockoutRiskCount = 0;
                int reorderRequiredCount = 0;
                int overstockedCount = 0;
                int slowMovingCount = 0;
                int healthyCount = 0;

                double totalRecommendedOrderQuantity = 0;

                Map<String, Object> highestPriorityProduct = null;

                for (Map<String, Object> insight : insights) {

                        String status = String.valueOf(insight.get("intelligenceStatus"));

                        switch (status) {

                                case "STOCKOUT_RISK":
                                        stockoutRiskCount++;
                                        break;

                                case "REORDER_REQUIRED":
                                        reorderRequiredCount++;
                                        break;

                                case "OVERSTOCKED":
                                        overstockedCount++;
                                        break;

                                case "SLOW_MOVING":
                                        slowMovingCount++;
                                        break;

                                case "HEALTHY":
                                        healthyCount++;
                                        break;

                                default:
                                        break;
                        }

                        totalRecommendedOrderQuantity += getDouble(
                                        insight.get("recommendedOrderQuantity"));
                }

                if (!insights.isEmpty()) {
                        highestPriorityProduct = insights.get(0);
                }

                Map<String, Object> summary = new HashMap<>();

                summary.put(
                                "totalProductsAnalyzed",
                                insights.size());

                summary.put(
                                "stockoutRiskCount",
                                stockoutRiskCount);

                summary.put(
                                "reorderRequiredCount",
                                reorderRequiredCount);

                summary.put(
                                "overstockedCount",
                                overstockedCount);

                summary.put(
                                "slowMovingCount",
                                slowMovingCount);

                summary.put(
                                "healthyCount",
                                healthyCount);

                summary.put(
                                "totalRecommendedOrderQuantity",
                                totalRecommendedOrderQuantity);

                summary.put(
                                "highestPriorityProduct",
                                highestPriorityProduct);

                return summary;
        }

        // =========================================================
        // SAFE NUMBER CONVERSION
        // =========================================================

        private double getDouble(Object value) {

                if (value == null) {
                        return 0.0;
                }

                if (value instanceof Number number) {
                        return number.doubleValue();
                }

                return Double.parseDouble(value.toString());
        }
}