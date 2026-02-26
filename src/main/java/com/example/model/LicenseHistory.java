package com.example.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "license_history")
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "license_id", nullable = false)
    private UUID licenseId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String status;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(length = 1000)
    private String description;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLicenseId() { return licenseId; }
    public void setLicenseId(UUID licenseId) { this.licenseId = licenseId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}