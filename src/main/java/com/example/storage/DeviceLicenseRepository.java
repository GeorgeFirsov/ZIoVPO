package com.example.storage;

import com.example.model.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    long countByLicenseId(UUID licenseId);

    DeviceLicense findByLicenseIdAndDeviceId(UUID licenseId, UUID deviceId);
}