package com.example.storage;

import com.example.model.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
}