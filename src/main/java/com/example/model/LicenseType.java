package com.example.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "license_type")
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_duration_in_days", nullable = false)
    private int defaultDurationInDays;

    @Column(length = 500)
    private String description;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDefaultDurationInDays() { return defaultDurationInDays; }
    public void setDefaultDurationInDays(int defaultDurationInDays) { this.defaultDurationInDays = defaultDurationInDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}