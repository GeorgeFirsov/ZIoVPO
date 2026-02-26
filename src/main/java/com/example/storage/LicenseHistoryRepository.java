package com.example.storage;

import com.example.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}