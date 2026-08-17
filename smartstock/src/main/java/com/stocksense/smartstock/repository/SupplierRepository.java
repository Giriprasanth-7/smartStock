package com.stocksense.smartstock.repository;

import com.stocksense.smartstock.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}