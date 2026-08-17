package com.stocksense.smartstock.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String movementType;

    private int quantity;

    private LocalDateTime movementDate;

    private String note;

    public StockMovement() {
    }

    public StockMovement(
            Long productId,
            String movementType,
            int quantity,
            LocalDateTime movementDate,
            String note) {

        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.movementDate = movementDate;
        this.note = note;
    }

    // =========================================================
    // AUTOMATIC MOVEMENT DATE
    // =========================================================

    @PrePersist
    public void setDefaultMovementDate() {

        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }

    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}