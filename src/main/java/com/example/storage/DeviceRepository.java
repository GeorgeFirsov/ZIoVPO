package com.example.storage;

import com.example.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Device findByMacAddress(String macAddress);
}