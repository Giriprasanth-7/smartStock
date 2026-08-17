package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReorderRecommendationService {

    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;

    public ReorderRecommendationService(
            ProductRepository productRepository,
            StockMovementService stockMovementService) {

        this.productRepository = productRepository;
        this.stockMovementService = stockMovementService;
    }

    // =========================================================
    // SMART REORDER RECOMMENDATIONS
    // =========================================================

    public List<Map<String, Object>> getSmartRecommendations() {

        List<Product> products = productRepository.findAll();

        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Product product : products) {

            if (product.getQuantity() == null) {
                continue;
            }

            if (product.getReorderLevel() == null) {
                continue;
            }

            // -------------------------------------------------
            // PRODUCT CONFIGURATION
            // -------------------------------------------------

            int currentStock = product.getQuantity();

            int configuredReorderLevel = product.getReorderLevel();

            int leadTimeDays = product.getLeadTimeDays() != null
                    ? product.getLeadTimeDays()
                    : 0;

            int safetyStock = product.getSafetyStock() != null
                    ? product.getSafetyStock()
                    : 0;

            int minimumOrderQuantity = product.getMinimumOrderQuantity() != null
                    ? product.getMinimumOrderQuantity()
                    : 0;

            Integer maximumStockLevel = product.getMaximumStockLevel();

            // -------------------------------------------------
            // DEMAND
            // -------------------------------------------------

            double averageDailyDemand = stockMovementService
                    .getAverageDailyDemand(
                            product.getId());

            // -------------------------------------------------
            // LEAD-TIME DEMAND
            // -------------------------------------------------

            double leadTimeDemand = averageDailyDemand * leadTimeDays;

            // -------------------------------------------------
            // DEMAND BASED REORDER POINT
            // -------------------------------------------------

            double demandBasedReorderPoint = leadTimeDemand + safetyStock;

            // -------------------------------------------------
            // EFFECTIVE REORDER POINT
            // -------------------------------------------------

            double effectiveReorderPoint = Math.max(
                    configuredReorderLevel,
                    demandBasedReorderPoint);

            // -------------------------------------------------
            // CHECK REORDER CONDITION
            // -------------------------------------------------

            if (currentStock <= effectiveReorderPoint) {

                int targetStock;

                // If maximum stock is configured,
                // use it as the target.
                if (maximumStockLevel != null
                        && maximumStockLevel > 0) {

                    targetStock = maximumStockLevel;

                } else {

                    targetStock = (int) Math.ceil(
                            effectiveReorderPoint);
                }

                // -------------------------------------------------
                // INITIAL ORDER QUANTITY
                // -------------------------------------------------

                int recommendedQuantity = Math.max(
                        0,
                        targetStock - currentStock);

                // -------------------------------------------------
                // APPLY MINIMUM ORDER QUANTITY
                // -------------------------------------------------

                if (minimumOrderQuantity > 0
                        && recommendedQuantity > 0
                        && recommendedQuantity < minimumOrderQuantity) {

                    recommendedQuantity = minimumOrderQuantity;
                }

                // -------------------------------------------------
                // RESPECT MAXIMUM STOCK LEVEL
                // -------------------------------------------------

                if (maximumStockLevel != null
                        && maximumStockLevel > 0) {

                    int maximumAllowedOrder = Math.max(
                            0,
                            maximumStockLevel
                                    - currentStock);

                    recommendedQuantity = Math.min(
                            recommendedQuantity,
                            maximumAllowedOrder);
                }

                // -------------------------------------------------
                // BUILD RESPONSE
                // -------------------------------------------------

                Map<String, Object> recommendation = new HashMap<>();

                recommendation.put(
                        "productId",
                        product.getId());

                recommendation.put(
                        "productName",
                        product.getName());

                recommendation.put(
                        "sku",
                        product.getSku());

                recommendation.put(
                        "currentStock",
                        currentStock);

                recommendation.put(
                        "configuredReorderLevel",
                        configuredReorderLevel);

                recommendation.put(
                        "averageDailyDemand",
                        Math.round(
                                averageDailyDemand * 100.0) / 100.0);

                recommendation.put(
                        "leadTimeDays",
                        leadTimeDays);

                recommendation.put(
                        "leadTimeDemand",
                        Math.ceil(
                                leadTimeDemand));

                recommendation.put(
                        "safetyStock",
                        safetyStock);

                recommendation.put(
                        "demandBasedReorderPoint",
                        Math.ceil(
                                demandBasedReorderPoint));

                recommendation.put(
                        "effectiveReorderPoint",
                        Math.ceil(
                                effectiveReorderPoint));

                recommendation.put(
                        "minimumOrderQuantity",
                        minimumOrderQuantity);

                recommendation.put(
                        "maximumStockLevel",
                        maximumStockLevel);

                recommendation.put(
                        "recommendedOrderQuantity",
                        recommendedQuantity);

                recommendation.put(
                        "status",
                        "REORDER REQUIRED");

                // -------------------------------------------------
                // EXPLANATION
                // -------------------------------------------------

                String reason;

                if (averageDailyDemand > 0
                        && leadTimeDays > 0) {

                    reason = "Reorder recommended because "
                            + "current stock is at or below "
                            + "the demand-based reorder point. "
                            + "Lead-time demand and safety "
                            + "stock are considered.";

                } else {

                    reason = "Reorder recommended because "
                            + "current stock is at or below "
                            + "the configured reorder level.";

                }

                recommendation.put(
                        "reason",
                        reason);

                recommendations.add(
                        recommendation);
            }
        }

        return recommendations;
    }
}