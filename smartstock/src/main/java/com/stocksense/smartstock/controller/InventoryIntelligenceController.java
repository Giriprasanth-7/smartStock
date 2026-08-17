package com.stocksense.smartstock.controller;

import com.stocksense.smartstock.service.InventoryIntelligenceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intelligence")
@CrossOrigin(origins = "*")
public class InventoryIntelligenceController {

    private final InventoryIntelligenceService intelligenceService;

    public InventoryIntelligenceController(
            InventoryIntelligenceService intelligenceService) {

        this.intelligenceService = intelligenceService;
    }

    // =========================================================
    // ANALYZE ALL PRODUCTS
    // =========================================================

    @GetMapping("/inventory-risk")
    public List<Map<String, Object>> analyzeAllProducts() {

        return intelligenceService.analyzeAllProducts();
    }

    // =========================================================
    // ANALYZE ONE PRODUCT
    // =========================================================

    @GetMapping("/inventory-risk/{productId}")
    public Map<String, Object> analyzeProduct(
            @PathVariable Long productId) {

        return intelligenceService.analyzeProduct(productId);
    }
}