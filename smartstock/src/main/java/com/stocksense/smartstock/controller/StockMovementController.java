package com.stocksense.smartstock.controller;

import com.stocksense.smartstock.entity.StockMovement;
import com.stocksense.smartstock.service.StockMovementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(
            StockMovementService stockMovementService) {

        this.stockMovementService = stockMovementService;
    }

    // =========================
    // GET ALL MOVEMENTS
    // =========================

    @GetMapping
    public List<StockMovement> getAllMovements() {

        return stockMovementService
                .getAllMovements();
    }

    // =========================
    // GET MOVEMENTS BY PRODUCT
    // =========================

    @GetMapping("/product/{productId}")
    public List<StockMovement> getMovementsByProduct(
            @PathVariable Long productId) {

        return stockMovementService
                .getMovementsByProduct(productId);
    }

    // =========================
    // GET TOTAL SALES
    // =========================

    @GetMapping("/product/{productId}/total-sales")
    public Integer getTotalSales(
            @PathVariable Long productId) {

        return stockMovementService
                .getTotalSales(productId);
    }

    // =========================
    // GET AVERAGE DAILY DEMAND
    // =========================

    @GetMapping("/product/{productId}/average-daily-demand")
    public double getAverageDailyDemand(
            @PathVariable Long productId) {

        return stockMovementService
                .getAverageDailyDemand(productId);
    }

    // =========================
    // GET DEMAND FORECAST
    // =========================

    @GetMapping("/product/{productId}/forecast")
    public Map<String, Object> getDemandForecast(
            @PathVariable Long productId) {

        return stockMovementService
                .getDemandForecast(productId);
    }

    // =========================
    // ADD STOCK MOVEMENT
    // =========================

    @PostMapping
    public StockMovement addMovement(
            @RequestBody StockMovement movement) {

        return stockMovementService
                .addMovement(movement);
    }

    // =========================
    // DELETE STOCK MOVEMENT
    // =========================

    @DeleteMapping("/{movementId}")
    public Map<String, String> deleteMovement(
            @PathVariable Long movementId) {

        stockMovementService
                .deleteMovement(movementId);

        return Map.of(
                "message",
                "Stock movement deleted successfully");
    }
}