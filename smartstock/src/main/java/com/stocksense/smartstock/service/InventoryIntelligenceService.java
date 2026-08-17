package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.repository.ProductRepository;
import com.stocksense.smartstock.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryIntelligenceService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final StockMovementRepository stockMovementRepository;

    public InventoryIntelligenceService(
            ProductRepository productRepository,
            ProductService productService,
            StockMovementRepository stockMovementRepository) {

        this.productRepository = productRepository;
        this.productService = productService;
        this.stockMovementRepository = stockMovementRepository;
    }

    // =========================================================
    // ANALYZE ALL PRODUCTS
    // =========================================================

    public List<Map<String, Object>> analyzeAllProducts() {

        List<Product> products = productRepository.findAll();

        List<Map<String, Object>> results = new ArrayList<>();

        for (Product product : products) {

            results.add(analyzeProduct(product.getId()));
        }

        return results;
    }

    // =========================================================
    // ANALYZE ONE PRODUCT
    // =========================================================

    public Map<String, Object> analyzeProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found"));

        // =====================================================
        // EXISTING SMART REPLENISHMENT ANALYSIS
        // =====================================================

        Map<String, Object> replenishment = productService.getSmartReplenishment(productId);

        int currentStock = ((Number) replenishment.get("currentStock"))
                .intValue();

        int safetyStock = ((Number) replenishment.get("safetyStock"))
                .intValue();

        double effectiveReorderPoint = ((Number) replenishment.get("effectiveReorderPoint"))
                .doubleValue();

        double averageDailyDemand = ((Number) replenishment.get("averageDailyDemand"))
                .doubleValue();

        int leadTimeDays = ((Number) replenishment.get("leadTimeDays"))
                .intValue();

        int minimumOrderQuantity = ((Number) replenishment.get("minimumOrderQuantity"))
                .intValue();

        int maximumStockLevel = ((Number) replenishment.get("maximumStockLevel"))
                .intValue();

        int existingRecommendedOrder = ((Number) replenishment.get("recommendedOrderQuantity"))
                .intValue();

        boolean supplierActive = Boolean.TRUE.equals(
                replenishment.get("supplierActive"));

        // =====================================================
        // DEMAND TREND ANALYSIS
        // =====================================================

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime recentStart = now.minusDays(7);

        LocalDateTime previousStart = now.minusDays(14);

        Integer recentSalesValue = stockMovementRepository.getSalesBetween(
                productId,
                recentStart,
                now);

        Integer previousSalesValue = stockMovementRepository.getSalesBetween(
                productId,
                previousStart,
                recentStart);

        int recentSales = recentSalesValue != null
                ? recentSalesValue
                : 0;

        int previousSales = previousSalesValue != null
                ? previousSalesValue
                : 0;

        double demandChangePercentage = 0.0;

        if (previousSales > 0) {

            demandChangePercentage = ((double) (recentSales - previousSales)
                    / previousSales) * 100.0;

        } else if (recentSales > 0) {

            demandChangePercentage = 100.0;
        }

        demandChangePercentage = Math.round(
                demandChangePercentage * 100.0)
                / 100.0;

        String demandTrend;

        if (demandChangePercentage >= 20) {

            demandTrend = "RISING";

        } else if (demandChangePercentage <= -20) {

            demandTrend = "FALLING";

        } else {

            demandTrend = "STABLE";
        }

        // =====================================================
        // DEMAND INSIGHT
        // =====================================================

        String demandInsight;

        if ("RISING".equals(demandTrend)) {

            demandInsight = "Recent demand is increasing. Inventory may be consumed faster than expected.";

        } else if ("FALLING".equals(demandTrend)) {

            demandInsight = "Recent demand is decreasing. Replenishment urgency may be lower.";

        } else {

            demandInsight = "Recent demand is relatively stable.";
        }

        // =====================================================
        // INVENTORY RISK SCORE
        // =====================================================

        int riskScore = 0;

        List<String> riskFactors = new ArrayList<>();

        // =====================================================
        // CURRENT STOCK FACTOR
        // =====================================================

        if (currentStock == 0) {

            riskScore += 40;

            riskFactors.add(
                    "Product is completely out of stock.");

        } else if (currentStock <= effectiveReorderPoint) {

            riskScore += 30;

            riskFactors.add(
                    "Current stock is at or below the reorder point.");

        } else if (currentStock <= effectiveReorderPoint + safetyStock) {

            riskScore += 15;

            riskFactors.add(
                    "Current stock is approaching the replenishment zone.");
        }

        // =====================================================
        // DEMAND COVERAGE FACTOR
        // =====================================================

        double daysOfStock = 0.0;

        if (averageDailyDemand > 0) {

            daysOfStock = currentStock / averageDailyDemand;

            if (daysOfStock <= leadTimeDays) {

                riskScore += 25;

                riskFactors.add(
                        "Current stock may not cover supplier lead time demand.");

            } else if (daysOfStock <= leadTimeDays + 3) {

                riskScore += 10;

                riskFactors.add(
                        "Inventory coverage is getting close to supplier lead time.");
            }
        }

        // =====================================================
        // SAFETY STOCK FACTOR
        // =====================================================

        if (currentStock < safetyStock) {

            riskScore += 20;

            riskFactors.add(
                    "Current stock is below the safety stock level.");
        }

        // =====================================================
        // SUPPLIER LEAD TIME FACTOR
        // =====================================================

        if (leadTimeDays >= 7) {

            riskScore += 10;

            riskFactors.add(
                    "Supplier lead time is relatively high.");
        }

        // =====================================================
        // SUPPLIER STATUS FACTOR
        // =====================================================

        if (!supplierActive) {

            riskScore += 15;

            riskFactors.add(
                    "No active supplier is available for this product.");
        }

        // =====================================================
        // RISING DEMAND FACTOR
        // =====================================================

        if ("RISING".equals(demandTrend)) {

            riskScore += 15;

            riskFactors.add(
                    "Recent demand is rising.");
        }

        // =====================================================
        // LIMIT RISK SCORE
        // =====================================================

        riskScore = Math.min(riskScore, 100);

        // =====================================================
        // RISK LEVEL
        // =====================================================

        String riskLevel;

        if (riskScore >= 70) {

            riskLevel = "CRITICAL";

        } else if (riskScore >= 45) {

            riskLevel = "HIGH";

        } else if (riskScore >= 20) {

            riskLevel = "MEDIUM";

        } else {

            riskLevel = "LOW";
        }

        // =====================================================
        // ADAPTIVE REPLENISHMENT
        // =====================================================

        int adaptiveRecommendedOrder = existingRecommendedOrder;

        String replenishmentUrgency;

        String adaptiveRecommendation;

        // =====================================================
        // CRITICAL
        // =====================================================

        if ("CRITICAL".equals(riskLevel)
                || currentStock == 0) {

            replenishmentUrgency = "IMMEDIATE";

            adaptiveRecommendation = "Inventory is critically low. Place an immediate replenishment order.";

            if (adaptiveRecommendedOrder == 0) {

                adaptiveRecommendedOrder = minimumOrderQuantity;
            }
        }

        // =====================================================
        // HIGH RISK
        // =====================================================

        else if ("HIGH".equals(riskLevel)) {

            replenishmentUrgency = "HIGH";

            adaptiveRecommendation = "Inventory risk is high. Replenish soon and closely monitor demand.";

            if (adaptiveRecommendedOrder == 0
                    && minimumOrderQuantity > 0) {

                adaptiveRecommendedOrder = minimumOrderQuantity;
            }
        }

        // =====================================================
        // RISING DEMAND
        // =====================================================

        else if ("RISING".equals(demandTrend)) {

            replenishmentUrgency = "HIGH";

            adaptiveRecommendation = "Demand is rising. Consider replenishing earlier than the normal reorder point.";

            if (adaptiveRecommendedOrder == 0
                    && currentStock <= effectiveReorderPoint + safetyStock) {

                int trendAdjustedOrder = (int) Math.ceil(
                        averageDailyDemand
                                * Math.max(leadTimeDays, 1));

                adaptiveRecommendedOrder = Math.max(
                        trendAdjustedOrder,
                        minimumOrderQuantity);
            }
        }

        // =====================================================
        // MEDIUM RISK
        // =====================================================

        else if ("MEDIUM".equals(riskLevel)) {

            replenishmentUrgency = "MEDIUM";

            adaptiveRecommendation = "Inventory requires monitoring. Prepare replenishment based on demand.";

        }

        // =====================================================
        // LOW RISK
        // =====================================================

        else {

            replenishmentUrgency = "LOW";

            adaptiveRecommendation = "Inventory position is healthy. No immediate replenishment is required.";
        }

        // =====================================================
        // MAXIMUM STOCK LIMIT
        // =====================================================

        if (maximumStockLevel > 0) {

            int maximumAllowedOrder = maximumStockLevel - currentStock;

            if (maximumAllowedOrder < 0) {

                maximumAllowedOrder = 0;
            }

            adaptiveRecommendedOrder = Math.min(
                    adaptiveRecommendedOrder,
                    maximumAllowedOrder);
        }

        // =====================================================
        // EXPECTED STOCK AFTER ORDER
        // =====================================================

        int expectedStockAfterOrder = currentStock + adaptiveRecommendedOrder;

        // =====================================================
        // ROUND DAYS OF STOCK
        // =====================================================

        daysOfStock = Math.round(
                daysOfStock * 100.0)
                / 100.0;

        // =====================================================
        // RESPONSE
        // =====================================================

        Map<String, Object> result = new HashMap<>();

        // =====================================================
        // PRODUCT
        // =====================================================

        result.put(
                "productId",
                product.getId());

        result.put(
                "productName",
                product.getName());

        result.put(
                "sku",
                product.getSku());

        // =====================================================
        // INVENTORY
        // =====================================================

        result.put(
                "currentStock",
                currentStock);

        result.put(
                "averageDailyDemand",
                averageDailyDemand);

        result.put(
                "daysOfStock",
                daysOfStock);

        result.put(
                "safetyStock",
                safetyStock);

        result.put(
                "effectiveReorderPoint",
                effectiveReorderPoint);

        result.put(
                "leadTimeDays",
                leadTimeDays);

        // =====================================================
        // DEMAND TREND
        // =====================================================

        result.put(
                "recent7DaySales",
                recentSales);

        result.put(
                "previous7DaySales",
                previousSales);

        result.put(
                "demandChangePercentage",
                demandChangePercentage);

        result.put(
                "demandTrend",
                demandTrend);

        result.put(
                "demandInsight",
                demandInsight);

        // =====================================================
        // SUPPLIER
        // =====================================================

        result.put(
                "supplierActive",
                supplierActive);

        // =====================================================
        // RISK
        // =====================================================

        result.put(
                "riskScore",
                riskScore);

        result.put(
                "riskLevel",
                riskLevel);

        result.put(
                "riskFactors",
                riskFactors);

        // =====================================================
        // REPLENISHMENT
        // =====================================================

        result.put(
                "existingRecommendedOrderQuantity",
                existingRecommendedOrder);

        result.put(
                "adaptiveRecommendedOrderQuantity",
                adaptiveRecommendedOrder);

        result.put(
                "replenishmentUrgency",
                replenishmentUrgency);

        result.put(
                "adaptiveRecommendation",
                adaptiveRecommendation);

        result.put(
                "expectedStockAfterOrder",
                expectedStockAfterOrder);

        result.put(
                "minimumOrderQuantity",
                minimumOrderQuantity);

        result.put(
                "maximumStockLevel",
                maximumStockLevel);

        return result;
    }
}