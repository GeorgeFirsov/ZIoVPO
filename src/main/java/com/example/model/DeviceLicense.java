package com.example.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "device_license")
public class DeviceLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "license_id", nullable = false)
    private UUID licenseId;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLicenseId() { return licenseId; }
    public void setLicenseId(UUID licenseId) { this.licenseId = licenseId; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public LocalDate getActivationDate() { return activationDate; }
    public void setActivationDate(LocalDate activationDate) { this.activationDate = activationDate; }
}