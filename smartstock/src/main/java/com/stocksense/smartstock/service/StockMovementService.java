package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.entity.StockMovement;
import com.stocksense.smartstock.repository.ProductRepository;
import com.stocksense.smartstock.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockMovementService {

        private final StockMovementRepository stockMovementRepository;
        private final ProductRepository productRepository;

        public StockMovementService(
                        StockMovementRepository stockMovementRepository,
                        ProductRepository productRepository) {

                this.stockMovementRepository = stockMovementRepository;
                this.productRepository = productRepository;
        }

        // =========================
        // ADD STOCK MOVEMENT
        // =========================

        @Transactional
        public StockMovement addMovement(StockMovement movement) {

                // Validate product
                Product product = productRepository
                                .findById(movement.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                // Validate quantity
                if (movement.getQuantity() <= 0) {
                        throw new RuntimeException(
                                        "Movement quantity must be greater than zero");
                }

                // Automatically set transaction date
                if (movement.getMovementDate() == null) {
                        movement.setMovementDate(LocalDateTime.now());
                }

                // =========================
                // SALE
                // =========================

                if ("SALE".equalsIgnoreCase(
                                movement.getMovementType())) {

                        if (product.getQuantity() == null) {
                                throw new RuntimeException(
                                                "Product stock quantity is not configured");
                        }

                        if (product.getQuantity() < movement.getQuantity()) {
                                throw new RuntimeException(
                                                "Insufficient stock. Available stock: "
                                                                + product.getQuantity());
                        }

                        product.setQuantity(
                                        product.getQuantity()
                                                        - movement.getQuantity());
                }

                // =========================
                // PURCHASE
                // =========================

                else if ("PURCHASE".equalsIgnoreCase(
                                movement.getMovementType())) {

                        int currentQuantity = product.getQuantity() != null
                                        ? product.getQuantity()
                                        : 0;

                        product.setQuantity(
                                        currentQuantity
                                                        + movement.getQuantity());
                }

                // =========================
                // INVALID TYPE
                // =========================

                else {

                        throw new RuntimeException(
                                        "Invalid movement type. Use PURCHASE or SALE");
                }

                // Save updated product
                productRepository.save(product);

                // Save transaction
                return stockMovementRepository.save(movement);
        }

        // =========================
        // GET ALL MOVEMENTS
        // =========================

        public List<StockMovement> getAllMovements() {

                return stockMovementRepository.findAll();
        }

        // =========================
        // GET MOVEMENTS BY PRODUCT
        // =========================

        public List<StockMovement> getMovementsByProduct(
                        Long productId) {

                return stockMovementRepository
                                .findByProductId(productId);
        }

        // =========================
        // GET TOTAL SALES
        // =========================

        public Integer getTotalSales(Long productId) {

                Integer totalSales = stockMovementRepository
                                .getTotalSalesByProductId(productId);

                return totalSales != null
                                ? totalSales
                                : 0;
        }

        // =========================
        // AVERAGE DAILY DEMAND
        // =========================

        public double getAverageDailyDemand(
                        Long productId) {

                LocalDateTime startDate = LocalDateTime.now().minusDays(30);

                Integer totalSales = stockMovementRepository.getSalesSince(
                                productId,
                                startDate);

                if (totalSales == null) {
                        totalSales = 0;
                }

                return totalSales / 30.0;
        }

        // =========================
        // DEMAND FORECAST
        // =========================

        public Map<String, Object> getDemandForecast(
                        Long productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Product not found"));

                double averageDailyDemand = getAverageDailyDemand(productId);

                double forecast7Days = averageDailyDemand * 7;

                double forecast30Days = averageDailyDemand * 30;

                double forecast90Days = averageDailyDemand * 90;

                Map<String, Object> forecast = new HashMap<>();

                forecast.put(
                                "productId",
                                product.getId());

                forecast.put(
                                "productName",
                                product.getName());

                forecast.put(
                                "sku",
                                product.getSku());

                forecast.put(
                                "historicalPeriodDays",
                                30);

                forecast.put(
                                "averageDailyDemand",
                                averageDailyDemand);

                forecast.put(
                                "forecastNext7Days",
                                Math.ceil(forecast7Days));

                forecast.put(
                                "forecastNext30Days",
                                Math.ceil(forecast30Days));

                forecast.put(
                                "forecastNext90Days",
                                Math.ceil(forecast90Days));

                forecast.put(
                                "currentStock",
                                product.getQuantity());

                return forecast;
        }

        // =========================
        // DELETE STOCK MOVEMENT
        // =========================

        @Transactional
        public void deleteMovement(
                        Long movementId) {

                // Find movement
                StockMovement movement = stockMovementRepository
                                .findById(movementId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Stock movement not found"));

                // Find product
                Product product = productRepository
                                .findById(movement.getProductId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Product not found"));

                int currentQuantity = product.getQuantity() != null
                                ? product.getQuantity()
                                : 0;

                // =========================
                // REVERSE SALE
                // =========================

                if ("SALE".equalsIgnoreCase(
                                movement.getMovementType())) {

                        product.setQuantity(
                                        currentQuantity
                                                        + movement.getQuantity());
                }

                // =========================
                // REVERSE PURCHASE
                // =========================

                else if ("PURCHASE".equalsIgnoreCase(
                                movement.getMovementType())) {

                        if (currentQuantity < movement.getQuantity()) {

                                throw new RuntimeException(
                                                "Cannot delete purchase movement because "
                                                                + "current stock is lower than "
                                                                + "the purchase quantity");
                        }

                        product.setQuantity(
                                        currentQuantity
                                                        - movement.getQuantity());
                }

                // =========================
                // INVALID TYPE
                // =========================

                else {

                        throw new RuntimeException(
                                        "Invalid movement type");
                }

                // Save corrected product stock
                productRepository.save(product);

                // Delete movement
                stockMovementRepository.delete(movement);
        }
}