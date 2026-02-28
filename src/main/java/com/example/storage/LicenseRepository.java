package com.example.storage;

import com.example.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    boolean existsByCode(String code);
    License findByCode(String code);

    @Query(value = "select l.* " +
            "from license l " +
            "join device_license dl on dl.license_id = l.id " +
            "where dl.device_id = :deviceId " +
            "and l.user_id = :userId " +
            "and l.product_id = :productId " +
            "and l.blocked = false " +
            "and l.ending_date >= current_date",
            nativeQuery = true)
    License findActiveByDeviceUserAndProduct(@Param("deviceId") UUID deviceId,
                                             @Param("userId") UUID userId,
                                             @Param("productId") UUID productId);
}