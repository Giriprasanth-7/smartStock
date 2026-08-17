package com.stocksense.smartstock.controller;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // =========================================================
    // GET ALL PRODUCTS
    // =========================================================

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Product getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with id: " + id));
    }

    // =========================================================
    // ADD PRODUCT
    // =========================================================

    @PostMapping
    public Product addProduct(
            @RequestBody Product product) {

        return productService.addProduct(product);
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        return productService.updateProduct(id, product);
    }

    // =========================================================
    // ASSIGN SUPPLIER TO PRODUCT
    // =========================================================

    @PutMapping("/{productId}/supplier/{supplierId}")
    public Product assignSupplier(
            @PathVariable Long productId,
            @PathVariable Long supplierId) {

        return productService.assignSupplier(
                productId,
                supplierId);
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return "Product deleted successfully";
    }

    // =========================================================
    // LOW STOCK PRODUCTS
    // =========================================================

    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts() {

        return productService.getLowStockProducts();
    }

    // =========================================================
    // SMART REORDER RECOMMENDATIONS
    // =========================================================

    @GetMapping("/reorder-recommendations")
    public List<Map<String, Object>> getReorderRecommendations() {

        return productService.getReorderRecommendations();
    }

    // =========================================================
    // SMART REPLENISHMENT FOR ONE PRODUCT
    // =========================================================

    @GetMapping("/{id}/replenishment")
    public Map<String, Object> getSmartReplenishment(
            @PathVariable Long id) {

        return productService.getSmartReplenishment(id);
    }

    // =========================================================
    // INVENTORY ALERTS
    // =========================================================

    @GetMapping("/inventory-alerts")
    public List<Map<String, Object>> getInventoryAlerts() {

        return productService.getInventoryAlerts();
    }

    // =========================================================
    // INVENTORY VALUE
    // =========================================================

    @GetMapping("/inventory-value")
    public Map<String, Object> getInventoryValue() {

        return productService.getInventoryValue();
    }

    // =========================================================
    // INVENTORY FINANCIAL SUMMARY
    // =========================================================

    @GetMapping("/financial-summary")
    public Map<String, Object> getInventoryFinancialSummary() {

        return productService.getInventoryFinancialSummary();
    }
}