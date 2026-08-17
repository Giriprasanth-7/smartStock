package com.stocksense.smartstock.service;

import com.stocksense.smartstock.entity.Supplier;
import com.stocksense.smartstock.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // =========================================================
    // ADD SUPPLIER
    // =========================================================

    public Supplier addSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    // =========================================================
    // GET ALL SUPPLIERS
    // =========================================================

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // =========================================================
    // GET SUPPLIER BY ID
    // =========================================================

    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    // =========================================================
    // UPDATE SUPPLIER
    // =========================================================

    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {

        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        existingSupplier.setName(updatedSupplier.getName());
        existingSupplier.setContactPerson(updatedSupplier.getContactPerson());
        existingSupplier.setEmail(updatedSupplier.getEmail());
        existingSupplier.setPhone(updatedSupplier.getPhone());
        existingSupplier.setAddress(updatedSupplier.getAddress());
        existingSupplier.setLeadTimeDays(updatedSupplier.getLeadTimeDays());
        existingSupplier.setActive(updatedSupplier.getActive());

        return supplierRepository.save(existingSupplier);
    }

    // =========================================================
    // DELETE SUPPLIER
    // =========================================================

    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found");
        }

        supplierRepository.deleteById(id);
    }
}