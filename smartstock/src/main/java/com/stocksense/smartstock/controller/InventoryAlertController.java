package com.stocksense.smartstock.controller;

import com.stocksense.smartstock.service.InventoryAlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    public InventoryAlertController(
            InventoryAlertService inventoryAlertService) {

        this.inventoryAlertService = inventoryAlertService;
    }

    @GetMapping
    public List<Map<String, Object>> getInventoryAlerts() {

        return inventoryAlertService.getInventoryAlerts();
    }
}