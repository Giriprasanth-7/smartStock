package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Product;
import com.stocksense.smartstock.entity.StockMovement;
import com.stocksense.smartstock.repository.ProductRepository;
import com.stocksense.smartstock.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public AnalyticsService(
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {

        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Map<String, Object> getInventoryDashboard() {

        List<Product> products = productRepository.findAll();
        List<StockMovement> movements = stockMovementRepository.findAll();

        int totalProducts = products.size();

        int totalUnitsInStock = products.stream()
                .filter(product -> product.getQuantity() != null)
                .mapToInt(Product::getQuantity)
                .sum();

        double totalInventoryValue = products.stream()
                .filter(product -> product.getQuantity() != null
                        && product.getPrice() != null)
                .mapToDouble(product -> product.getQuantity() * product.getPrice())
                .sum();

        long lowStockProducts = products.stream()
                .filter(product -> product.getQuantity() != null
                        && product.getLowStockThreshold() != null
                        && product.getQuantity() <= product.getLowStockThreshold())
                .count();

        long outOfStockProducts = products.stream()
                .filter(product -> product.getQuantity() != null
                        && product.getQuantity() == 0)
                .count();

        long purchaseMovements = movements.stream()
                .filter(movement -> "PURCHASE".equalsIgnoreCase(
                        movement.getMovementType()))
                .count();

        long saleMovements = movements.stream()
                .filter(movement -> "SALE".equalsIgnoreCase(
                        movement.getMovementType()))
                .count();

        int totalPurchasedUnits = movements.stream()
                .filter(movement -> "PURCHASE".equalsIgnoreCase(
                        movement.getMovementType()))
                .mapToInt(StockMovement::getQuantity)
                .sum();

        int totalSoldUnits = movements.stream()
                .filter(movement -> "SALE".equalsIgnoreCase(
                        movement.getMovementType()))
                .mapToInt(StockMovement::getQuantity)
                .sum();

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put(
                "totalProducts",
                totalProducts);

        dashboard.put(
                "totalUnitsInStock",
                totalUnitsInStock);

        dashboard.put(
                "totalInventoryValue",
                totalInventoryValue);

        dashboard.put(
                "lowStockProducts",
                lowStockProducts);

        dashboard.put(
                "outOfStockProducts",
                outOfStockProducts);

        dashboard.put(
                "purchaseMovements",
                purchaseMovements);

        dashboard.put(
                "saleMovements",
                saleMovements);

        dashboard.put(
                "totalPurchasedUnits",
                totalPurchasedUnits);

        dashboard.put(
                "totalSoldUnits",
                totalSoldUnits);

        return dashboard;
    }
}