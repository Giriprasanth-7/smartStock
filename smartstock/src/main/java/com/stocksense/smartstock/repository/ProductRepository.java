package com.stocksense.smartstock.repository;

import com.stocksense.smartstock.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByQuantityLessThanEqual(int quantity);

    boolean existsBySku(String sku);

}
