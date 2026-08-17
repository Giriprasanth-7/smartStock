package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.entity.Supplier;
import com.stocksense.smartstock.repository.ProductRepository;
import com.stocksense.smartstock.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

        private final ProductRepository productRepository;
        private final SupplierRepository supplierRepository;
        private final StockMovementService stockMovementService;

        public ProductService(
                        ProductRepository productRepository,
                        SupplierRepository supplierRepository,
                        StockMovementService stockMovementService) {

                this.productRepository = productRepository;
                this.supplierRepository = supplierRepository;
                this.stockMovementService = stockMovementService;
        }

        // =========================================================
        // ADD PRODUCT
        // =========================================================

        public Product addProduct(Product product) {
                return productRepository.save(product);
        }

        // =========================================================
        // GET ALL PRODUCTS
        // =========================================================

        public List<Product> getAllProducts() {
                return productRepository.findAll();
        }

        // =========================================================
        // GET PRODUCT BY ID
        // =========================================================

        public Optional<Product> getProductById(Long id) {
                return productRepository.findById(id);
        }

        // =========================================================
        // UPDATE COMPLETE PRODUCT
        // =========================================================

        public Product updateProduct(
                        Long id,
                        Product updatedProduct) {

                Product existingProduct = productRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                existingProduct.setName(updatedProduct.getName());
                existingProduct.setSku(updatedProduct.getSku());
                existingProduct.setDescription(updatedProduct.getDescription());
                existingProduct.setPrice(updatedProduct.getPrice());
                existingProduct.setQuantity(updatedProduct.getQuantity());

                existingProduct.setLowStockThreshold(
                                updatedProduct.getLowStockThreshold());

                existingProduct.setReorderLevel(
                                updatedProduct.getReorderLevel());

                existingProduct.setLeadTimeDays(
                                updatedProduct.getLeadTimeDays());

                existingProduct.setSafetyStock(
                                updatedProduct.getSafetyStock());

                existingProduct.setMinimumOrderQuantity(
                                updatedProduct.getMinimumOrderQuantity());

                existingProduct.setMaximumStockLevel(
                                updatedProduct.getMaximumStockLevel());

                /*
                 * Preserve supplier relationship when the updated
                 * product already contains a supplier.
                 */
                if (updatedProduct.getSupplier() != null
                                && updatedProduct.getSupplier().getId() != null) {

                        Supplier supplier = supplierRepository
                                        .findById(updatedProduct.getSupplier().getId())
                                        .orElseThrow(() -> new RuntimeException("Supplier not found"));

                        existingProduct.setSupplier(supplier);
                }

                return productRepository.save(existingProduct);
        }

        // =========================================================
        // ASSIGN SUPPLIER TO PRODUCT
        // =========================================================

        public Product assignSupplier(
                        Long productId,
                        Long supplierId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Supplier supplier = supplierRepository.findById(supplierId)
                                .orElseThrow(() -> new RuntimeException("Supplier not found"));

                product.setSupplier(supplier);

                return productRepository.save(product);
        }

        // =========================================================
        // SAVE PRODUCT
        // =========================================================

        public Product saveProduct(Product product) {
                return productRepository.save(product);
        }

        // =========================================================
        // DELETE PRODUCT
        // =========================================================

        public void deleteProduct(Long id) {
                productRepository.deleteById(id);
        }

        // =========================================================
        // LOW STOCK PRODUCTS
        // =========================================================

        public List<Product> getLowStockProducts() {

                List<Product> allProducts = productRepository.findAll();

                return allProducts.stream()
                                .filter(product -> product.getQuantity() != null
                                                && product.getLowStockThreshold() != null
                                                && product.getQuantity() <= product.getLowStockThreshold())
                                .toList();
        }

        // =========================================================
        // SMART REORDER RECOMMENDATIONS
        // =========================================================

        public List<Map<String, Object>> getReorderRecommendations() {

                List<Product> products = productRepository.findAll();

                List<Map<String, Object>> recommendations = new ArrayList<>();

                for (Product product : products) {

                        Map<String, Object> replenishment = getSmartReplenishment(product.getId());

                        double effectiveReorderPoint = ((Number) replenishment.get(
                                        "effectiveReorderPoint"))
                                        .doubleValue();

                        int currentStock = ((Number) replenishment.get(
                                        "currentStock"))
                                        .intValue();

                        if (currentStock <= effectiveReorderPoint) {
                                recommendations.add(replenishment);
                        }
                }

                return recommendations;
        }

        // =========================================================
        // SMART REPLENISHMENT ENGINE
        // =========================================================

        public Map<String, Object> getSmartReplenishment(
                        Long productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                // =====================================================
                // INVENTORY VALUES
                // =====================================================

                int currentStock = product.getQuantity() != null
                                ? product.getQuantity()
                                : 0;

                int reorderLevel = product.getReorderLevel() != null
                                ? product.getReorderLevel()
                                : 0;

                int safetyStock = product.getSafetyStock() != null
                                ? product.getSafetyStock()
                                : 0;

                int leadTimeDays = product.getLeadTimeDays() != null
                                ? product.getLeadTimeDays()
                                : 0;

                int minimumOrderQuantity = product.getMinimumOrderQuantity() != null
                                ? product.getMinimumOrderQuantity()
                                : 0;

                int maximumStockLevel = product.getMaximumStockLevel() != null
                                ? product.getMaximumStockLevel()
                                : 0;

                // =====================================================
                // DEMAND ANALYSIS
                // =====================================================

                double averageDailyDemand = stockMovementService.getAverageDailyDemand(
                                productId);

                // =====================================================
                // LEAD TIME DEMAND
                // =====================================================

                double leadTimeDemand = averageDailyDemand * leadTimeDays;

                // =====================================================
                // DEMAND-BASED REORDER POINT
                // =====================================================

                double demandBasedReorderPoint = leadTimeDemand + safetyStock;

                // =====================================================
                // EFFECTIVE REORDER POINT
                // =====================================================

                double effectiveReorderPoint = Math.max(
                                reorderLevel,
                                demandBasedReorderPoint);

                // =====================================================
                // TARGET STOCK
                // =====================================================

                int targetStock = reorderLevel + safetyStock;

                if (maximumStockLevel > 0) {

                        targetStock = Math.min(
                                        targetStock,
                                        maximumStockLevel);
                }

                // =====================================================
                // INITIAL ORDER QUANTITY
                // =====================================================

                int recommendedOrderQuantity = Math.max(
                                0,
                                targetStock - currentStock);

                // =====================================================
                // MINIMUM ORDER QUANTITY
                // =====================================================

                if (recommendedOrderQuantity > 0
                                && minimumOrderQuantity > 0
                                && recommendedOrderQuantity < minimumOrderQuantity) {

                        recommendedOrderQuantity = minimumOrderQuantity;
                }

                // =====================================================
                // MAXIMUM STOCK LIMIT
                // =====================================================

                if (maximumStockLevel > 0) {

                        int maximumAllowedOrder = maximumStockLevel - currentStock;

                        if (maximumAllowedOrder < 0) {
                                maximumAllowedOrder = 0;
                        }

                        recommendedOrderQuantity = Math.min(
                                        recommendedOrderQuantity,
                                        maximumAllowedOrder);
                }

                // =====================================================
                // STATUS
                // =====================================================

                String status;

                if (currentStock == 0) {

                        status = "OUT OF STOCK";

                } else if (currentStock <= effectiveReorderPoint) {

                        status = "REORDER REQUIRED";

                } else if (currentStock <= effectiveReorderPoint + safetyStock) {

                        status = "MONITOR";

                } else {

                        status = "STOCK HEALTHY";
                }

                // =====================================================
                // RESPONSE
                // =====================================================

                Map<String, Object> result = new HashMap<>();

                result.put(
                                "productId",
                                product.getId());

                result.put(
                                "productName",
                                product.getName());

                result.put(
                                "sku",
                                product.getSku());

                result.put(
                                "currentStock",
                                currentStock);

                result.put(
                                "reorderLevel",
                                reorderLevel);

                result.put(
                                "safetyStock",
                                safetyStock);

                result.put(
                                "targetStock",
                                targetStock);

                result.put(
                                "leadTimeDays",
                                leadTimeDays);

                result.put(
                                "minimumOrderQuantity",
                                minimumOrderQuantity);

                result.put(
                                "maximumStockLevel",
                                maximumStockLevel);

                // =====================================================
                // SUPPLIER INFORMATION
                // =====================================================

                if (product.getSupplier() != null) {

                        result.put(
                                        "supplierId",
                                        product.getSupplier().getId());

                        result.put(
                                        "supplierName",
                                        product.getSupplier().getName());

                        result.put(
                                        "supplierLeadTimeDays",
                                        product.getSupplier().getLeadTimeDays());

                        result.put(
                                        "supplierActive",
                                        product.getSupplier().getActive());
                } else {

                        result.put(
                                        "supplierId",
                                        null);

                        result.put(
                                        "supplierName",
                                        null);

                        result.put(
                                        "supplierLeadTimeDays",
                                        null);

                        result.put(
                                        "supplierActive",
                                        false);
                }

                // =====================================================
                // DEMAND VALUES
                // =====================================================

                result.put(
                                "averageDailyDemand",
                                averageDailyDemand);

                result.put(
                                "leadTimeDemand",
                                leadTimeDemand);

                result.put(
                                "demandBasedReorderPoint",
                                demandBasedReorderPoint);

                result.put(
                                "effectiveReorderPoint",
                                effectiveReorderPoint);

                // =====================================================
                // RECOMMENDATION
                // =====================================================

                result.put(
                                "recommendedOrderQuantity",
                                recommendedOrderQuantity);

                result.put(
                                "status",
                                status);

                return result;
        }

        // =========================================================
        // INVENTORY ALERT ENGINE
        // =========================================================

        public List<Map<String, Object>> getInventoryAlerts() {

                List<Product> products = productRepository.findAll();

                List<Map<String, Object>> alerts = new ArrayList<>();

                for (Product product : products) {

                        Map<String, Object> replenishment = getSmartReplenishment(
                                        product.getId());

                        int currentStock = ((Number) replenishment.get(
                                        "currentStock"))
                                        .intValue();

                        double effectiveReorderPoint = ((Number) replenishment.get(
                                        "effectiveReorderPoint"))
                                        .doubleValue();

                        int safetyStock = ((Number) replenishment.get(
                                        "safetyStock"))
                                        .intValue();

                        String severity = null;
                        String status = null;
                        String reason = null;
                        String recommendedAction = null;

                        // =================================================
                        // CRITICAL — OUT OF STOCK
                        // =================================================

                        if (currentStock == 0) {

                                severity = "CRITICAL";

                                status = "OUT OF STOCK";

                                reason = "Current stock has reached zero.";

                                recommendedAction = "Place an immediate replenishment order.";

                        }

                        // =================================================
                        // HIGH — REORDER REQUIRED
                        // =================================================

                        else if (currentStock <= effectiveReorderPoint) {

                                severity = "HIGH";

                                status = "REORDER REQUIRED";

                                reason = "Current stock is below the effective reorder point.";

                                recommendedAction = "Place a replenishment order.";

                        }

                        // =================================================
                        // MEDIUM — MONITOR
                        // =================================================

                        else if (currentStock <= effectiveReorderPoint
                                        + safetyStock) {

                                severity = "MEDIUM";

                                status = "MONITOR";

                                reason = "Current stock is approaching the replenishment zone.";

                                recommendedAction = "Monitor demand and prepare for replenishment.";
                        }

                        // =================================================
                        // ADD ALERT
                        // =================================================

                        if (status != null) {

                                Map<String, Object> alert = new HashMap<>();

                                alert.put(
                                                "productId",
                                                product.getId());

                                alert.put(
                                                "productName",
                                                product.getName());

                                alert.put(
                                                "sku",
                                                product.getSku());

                                alert.put(
                                                "currentStock",
                                                replenishment.get(
                                                                "currentStock"));

                                alert.put(
                                                "reorderLevel",
                                                replenishment.get(
                                                                "reorderLevel"));

                                alert.put(
                                                "safetyStock",
                                                replenishment.get(
                                                                "safetyStock"));

                                alert.put(
                                                "leadTimeDays",
                                                replenishment.get(
                                                                "leadTimeDays"));

                                alert.put(
                                                "averageDailyDemand",
                                                replenishment.get(
                                                                "averageDailyDemand"));

                                alert.put(
                                                "leadTimeDemand",
                                                replenishment.get(
                                                                "leadTimeDemand"));

                                alert.put(
                                                "demandBasedReorderPoint",
                                                replenishment.get(
                                                                "demandBasedReorderPoint"));

                                alert.put(
                                                "effectiveReorderPoint",
                                                replenishment.get(
                                                                "effectiveReorderPoint"));

                                alert.put(
                                                "recommendedOrderQuantity",
                                                replenishment.get(
                                                                "recommendedOrderQuantity"));

                                alert.put(
                                                "severity",
                                                severity);

                                alert.put(
                                                "status",
                                                status);

                                alert.put(
                                                "reason",
                                                reason);

                                alert.put(
                                                "recommendedAction",
                                                recommendedAction);

                                alerts.add(alert);
                        }
                }

                return alerts;
        }

        // =========================================================
        // INVENTORY VALUE
        // =========================================================

        public Map<String, Object> getInventoryValue() {

                List<Product> products = productRepository.findAll();

                double totalInventoryValue = 0.0;
                int totalUnits = 0;

                for (Product product : products) {

                        int quantity = product.getQuantity() != null
                                        ? product.getQuantity()
                                        : 0;

                        double price = 0.0;

                        if (product.getPrice() != null) {

                                price = ((Number) product.getPrice())
                                                .doubleValue();
                        }

                        totalUnits += quantity;

                        totalInventoryValue += quantity * price;
                }

                Map<String, Object> result = new HashMap<>();

                result.put(
                                "totalInventoryValue",
                                totalInventoryValue);

                result.put(
                                "totalUnits",
                                totalUnits);

                result.put(
                                "totalProducts",
                                products.size());

                return result;
        }

        // =========================================================
        // INVENTORY FINANCIAL SUMMARY
        // =========================================================

        public Map<String, Object> getInventoryFinancialSummary() {

                List<Product> products = productRepository.findAll();

                double totalInventoryValue = 0.0;
                double totalPotentialValue = 0.0;
                double averageProductValue = 0.0;

                int totalUnits = 0;
                int lowStockCount = 0;
                int outOfStockCount = 0;

                for (Product product : products) {

                        int quantity = product.getQuantity() != null
                                        ? product.getQuantity()
                                        : 0;

                        int maximumStock = product.getMaximumStockLevel() != null
                                        ? product.getMaximumStockLevel()
                                        : 0;

                        double price = 0.0;

                        if (product.getPrice() != null) {

                                price = ((Number) product.getPrice())
                                                .doubleValue();
                        }

                        // Current inventory value

                        double currentValue = quantity * price;

                        totalInventoryValue += currentValue;

                        totalUnits += quantity;

                        // Potential value at maximum stock

                        if (maximumStock > 0) {

                                totalPotentialValue += maximumStock * price;

                        } else {

                                totalPotentialValue += currentValue;
                        }

                        // Stock statistics

                        if (quantity == 0) {

                                outOfStockCount++;

                        } else if (product.getLowStockThreshold() != null
                                        && quantity <= product.getLowStockThreshold()) {

                                lowStockCount++;
                        }
                }

                if (!products.isEmpty()) {

                        averageProductValue = totalInventoryValue
                                        / products.size();
                }

                double inventoryGapValue = Math.max(
                                0.0,
                                totalPotentialValue
                                                - totalInventoryValue);

                Map<String, Object> result = new HashMap<>();

                result.put(
                                "totalProducts",
                                products.size());

                result.put(
                                "totalUnits",
                                totalUnits);

                result.put(
                                "totalInventoryValue",
                                totalInventoryValue);

                result.put(
                                "totalPotentialValue",
                                totalPotentialValue);

                result.put(
                                "inventoryGapValue",
                                inventoryGapValue);

                result.put(
                                "averageProductValue",
                                averageProductValue);

                result.put(
                                "lowStockCount",
                                lowStockCount);

                result.put(
                                "outOfStockCount",
                                outOfStockCount);

                return result;
        }
}
