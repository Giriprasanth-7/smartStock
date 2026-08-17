package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryAlertService {

    private final ProductRepository productRepository;

    public InventoryAlertService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Map<String, Object>> getInventoryAlerts() {

        List<Product> products = productRepository.findAll();

        List<Map<String, Object>> alerts = new ArrayList<>();

        for (Product product : products) {

            int currentStock = product.getQuantity();
            int lowStockThreshold = product.getLowStockThreshold();
            int reorderLevel = product.getReorderLevel();

            String status = "HEALTHY";
            String message = "Stock level is healthy.";

            if (currentStock == 0) {

                status = "OUT_OF_STOCK";
                message = "Product is completely out of stock.";

            } else if (currentStock <= reorderLevel) {

                status = "REORDER_REQUIRED";
                message = "Stock has reached the reorder level.";

            } else if (currentStock <= lowStockThreshold) {

                status = "LOW_STOCK";
                message = "Stock is below the safe threshold.";
            }

            if (!"HEALTHY".equals(status)) {

                Map<String, Object> alert = new HashMap<>();

                alert.put("productId", product.getId());
                alert.put("productName", product.getName());
                alert.put("sku", product.getSku());
                alert.put("currentStock", currentStock);
                alert.put("lowStockThreshold", lowStockThreshold);
                alert.put("reorderLevel", reorderLevel);
                alert.put("status", status);
                alert.put("message", message);

                alerts.add(alert);
            }
        }

        return alerts;
    }
}