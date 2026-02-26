package com.example.storage;

import com.example.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    boolean existsByCode(String code);
    License findByCode(String code);
}