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
        // VALIDATION
        // =========================================================

        private void validateProduct(Product product) {

                if (product == null) {
                        throw new IllegalArgumentException(
                                        "Product data cannot be null.");
                }

                // ---------------------------------------------------------
                // NAME
                // ---------------------------------------------------------

                if (product.getName() == null
                                || product.getName().trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Product name cannot be blank.");
                }

                // ---------------------------------------------------------
                // SKU
                // ---------------------------------------------------------

                if (product.getSku() == null
                                || product.getSku().trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Product SKU cannot be blank.");
                }

                // ---------------------------------------------------------
                // PRICE
                // ---------------------------------------------------------

                if (product.getPrice() != null
                                && product.getPrice() < 0) {

                        throw new IllegalArgumentException(
                                        "Product price cannot be negative.");
                }

                // ---------------------------------------------------------
                // QUANTITY
                // ---------------------------------------------------------

                if (product.getQuantity() != null
                                && product.getQuantity() < 0) {

                        throw new IllegalArgumentException(
                                        "Product quantity cannot be negative.");
                }

                // ---------------------------------------------------------
                // LOW STOCK THRESHOLD
                // ---------------------------------------------------------

                if (product.getLowStockThreshold() != null
                                && product.getLowStockThreshold() < 0) {

                        throw new IllegalArgumentException(
                                        "Low stock threshold cannot be negative.");
                }

                // ---------------------------------------------------------
                // REORDER LEVEL
                // ---------------------------------------------------------

                if (product.getReorderLevel() != null
                                && product.getReorderLevel() < 0) {

                        throw new IllegalArgumentException(
                                        "Reorder level cannot be negative.");
                }

                // ---------------------------------------------------------
                // LEAD TIME
                // ---------------------------------------------------------

                if (product.getLeadTimeDays() != null
                                && product.getLeadTimeDays() < 0) {

                        throw new IllegalArgumentException(
                                        "Lead time days cannot be negative.");
                }

                // ---------------------------------------------------------
                // SAFETY STOCK
                // ---------------------------------------------------------

                if (product.getSafetyStock() != null
                                && product.getSafetyStock() < 0) {

                        throw new IllegalArgumentException(
                                        "Safety stock cannot be negative.");
                }

                // ---------------------------------------------------------
                // MINIMUM ORDER QUANTITY
                // ---------------------------------------------------------

                if (product.getMinimumOrderQuantity() != null
                                && product.getMinimumOrderQuantity() < 0) {

                        throw new IllegalArgumentException(
                                        "Minimum order quantity cannot be negative.");
                }

                // ---------------------------------------------------------
                // MAXIMUM STOCK LEVEL
                // ---------------------------------------------------------

                if (product.getMaximumStockLevel() != null
                                && product.getMaximumStockLevel() < 0) {

                        throw new IllegalArgumentException(
                                        "Maximum stock level cannot be negative.");
                }

                // ---------------------------------------------------------
                // BUSINESS RULE:
                // REORDER LEVEL CANNOT EXCEED MAXIMUM STOCK
                // ---------------------------------------------------------

                if (product.getReorderLevel() != null
                                && product.getMaximumStockLevel() != null
                                && product.getReorderLevel() > product.getMaximumStockLevel()) {

                        throw new IllegalArgumentException(
                                        "Reorder level cannot exceed maximum stock level.");
                }

                // ---------------------------------------------------------
                // BUSINESS RULE:
                // MINIMUM ORDER QUANTITY CANNOT EXCEED MAXIMUM STOCK
                // ---------------------------------------------------------

                if (product.getMinimumOrderQuantity() != null
                                && product.getMaximumStockLevel() != null
                                && product.getMinimumOrderQuantity() > product.getMaximumStockLevel()) {

                        throw new IllegalArgumentException(
                                        "Minimum order quantity cannot exceed maximum stock level.");
                }
        }

        // =========================================================
        // ADD PRODUCT
        // =========================================================

        public Product addProduct(Product product) {

                validateProduct(product);

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

                if (id == null || id <= 0) {

                        throw new IllegalArgumentException(
                                        "Product ID must be a positive number.");
                }

                return productRepository.findById(id);
        }

        // =========================================================
        // UPDATE COMPLETE PRODUCT
        // =========================================================

        public Product updateProduct(
                        Long id,
                        Product updatedProduct) {

                if (id == null || id <= 0) {

                        throw new IllegalArgumentException(
                                        "Product ID must be a positive number.");
                }

                validateProduct(updatedProduct);

                Product existingProduct = productRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product not found with id: " + id));

                existingProduct.setName(
                                updatedProduct.getName());

                existingProduct.setSku(
                                updatedProduct.getSku());

                existingProduct.setDescription(
                                updatedProduct.getDescription());

                existingProduct.setPrice(
                                updatedProduct.getPrice());

                existingProduct.setQuantity(
                                updatedProduct.getQuantity());

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

                // ---------------------------------------------------------
                // SUPPLIER
                // ---------------------------------------------------------

                /*
                 * Preserve the existing supplier relationship when
                 * the update request does not contain a supplier.
                 *
                 * When a supplier ID is provided, verify that the
                 * supplier exists before assigning it.
                 */

                if (updatedProduct.getSupplier() != null
                                && updatedProduct.getSupplier().getId() != null) {

                        Supplier supplier = supplierRepository
                                        .findById(updatedProduct.getSupplier().getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Supplier not found with id: "
                                                                        + updatedProduct
                                                                                        .getSupplier()
                                                                                        .getId()));

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

                if (productId == null || productId <= 0) {

                        throw new IllegalArgumentException(
                                        "Product ID must be a positive number.");
                }

                if (supplierId == null || supplierId <= 0) {

                        throw new IllegalArgumentException(
                                        "Supplier ID must be a positive number.");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product not found with id: "
                                                                + productId));

                Supplier supplier = supplierRepository.findById(supplierId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Supplier not found with id: "
                                                                + supplierId));

                product.setSupplier(supplier);

                return productRepository.save(product);
        }

        // =========================================================
        // SAVE PRODUCT
        // =========================================================

        public Product saveProduct(Product product) {

                validateProduct(product);

                return productRepository.save(product);
        }

        // =========================================================
        // DELETE PRODUCT
        // =========================================================

        public void deleteProduct(Long id) {

                if (id == null || id <= 0) {

                        throw new IllegalArgumentException(
                                        "Product ID must be a positive number.");
                }

                if (!productRepository.existsById(id)) {

                        throw new IllegalArgumentException(
                                        "Product not found with id: " + id);
                }

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

                if (productId == null || productId <= 0) {

                        throw new IllegalArgumentException(
                                        "Product ID must be a positive number.");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product not found with id: "
                                                                + productId));

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

                int productLeadTimeDays = product.getLeadTimeDays() != null
                                ? product.getLeadTimeDays()
                                : 0;

                int minimumOrderQuantity = product.getMinimumOrderQuantity() != null
                                ? product.getMinimumOrderQuantity()
                                : 0;

                int maximumStockLevel = product.getMaximumStockLevel() != null
                                ? product.getMaximumStockLevel()
                                : 0;

                // =====================================================
                // SUPPLIER INFORMATION
                // =====================================================

                Supplier supplier = product.getSupplier();

                boolean supplierAvailable = supplier != null
                                && Boolean.TRUE.equals(
                                                supplier.getActive());

                Integer supplierLeadTimeDays = supplier != null
                                ? supplier.getLeadTimeDays()
                                : null;

                /*
                 * Effective lead time:
                 *
                 * 1. Use active supplier lead time when available.
                 * 2. Otherwise use product lead time.
                 */

                int effectiveLeadTimeDays = productLeadTimeDays;

                if (supplierAvailable
                                && supplierLeadTimeDays != null
                                && supplierLeadTimeDays >= 0) {

                        effectiveLeadTimeDays = supplierLeadTimeDays;
                }

                // =====================================================
                // DEMAND ANALYSIS
                // =====================================================

                double averageDailyDemand = stockMovementService
                                .getAverageDailyDemand(productId);

                // =====================================================
                // LEAD TIME DEMAND
                // =====================================================

                double leadTimeDemand = averageDailyDemand
                                * effectiveLeadTimeDays;

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

                int targetStock = (int) Math.ceil(
                                effectiveReorderPoint
                                                + safetyStock);

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
                                effectiveLeadTimeDays);

                result.put(
                                "productLeadTimeDays",
                                productLeadTimeDays);

                result.put(
                                "effectiveLeadTimeDays",
                                effectiveLeadTimeDays);

                result.put(
                                "minimumOrderQuantity",
                                minimumOrderQuantity);

                result.put(
                                "maximumStockLevel",
                                maximumStockLevel);

                // =====================================================
                // SUPPLIER INFORMATION
                // =====================================================

                if (supplier != null) {

                        result.put(
                                        "supplierId",
                                        supplier.getId());

                        result.put(
                                        "supplierName",
                                        supplier.getName());

                        result.put(
                                        "supplierLeadTimeDays",
                                        supplier.getLeadTimeDays());

                        result.put(
                                        "supplierActive",
                                        supplier.getActive());

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

                        else if (currentStock <= effectiveReorderPoint + safetyStock) {

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

                                price = product.getPrice().doubleValue();
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

                                price = product.getPrice().doubleValue();
                        }

                        // -------------------------------------------------
                        // CURRENT INVENTORY VALUE
                        // -------------------------------------------------

                        double currentValue = quantity * price;

                        totalInventoryValue += currentValue;

                        totalUnits += quantity;

                        // -------------------------------------------------
                        // POTENTIAL VALUE
                        // -------------------------------------------------

                        if (maximumStock > 0) {

                                totalPotentialValue += maximumStock * price;

                        } else {

                                totalPotentialValue += currentValue;
                        }

                        // -------------------------------------------------
                        // STOCK STATISTICS
                        // -------------------------------------------------

                        if (quantity == 0) {

                                outOfStockCount++;

                        } else if (product.getLowStockThreshold() != null
                                        && quantity <= product
                                                        .getLowStockThreshold()) {

                                lowStockCount++;
                        }
                }

                // -----------------------------------------------------
                // AVERAGE PRODUCT VALUE
                // -----------------------------------------------------

                if (!products.isEmpty()) {

                        averageProductValue = totalInventoryValue
                                        / products.size();
                }

                // -----------------------------------------------------
                // INVENTORY GAP
                // -----------------------------------------------------

                double inventoryGapValue = Math.max(
                                0.0,
                                totalPotentialValue
                                                - totalInventoryValue);

                // -----------------------------------------------------
                // RESPONSE
                // -----------------------------------------------------

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