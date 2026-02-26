package com.example.service;

import com.example.model.Device;
import com.example.model.DeviceLicense;
import com.example.model.License;
import com.example.model.LicenseHistory;
import com.example.storage.DeviceLicenseRepository;
import com.example.storage.LicenseHistoryRepository;
import com.example.storage.LicenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LicenseActivationTransactionService {

    private final LicenseRepository licenseRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;

    public LicenseActivationTransactionService(LicenseRepository licenseRepository,
                                               DeviceLicenseRepository deviceLicenseRepository,
                                               LicenseHistoryRepository licenseHistoryRepository) {
        this.licenseRepository = licenseRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
    }

    @Transactional
    public License firstActivation(License license, UUID userId, int defaultDurationDays, Device device) {
        LocalDate now = LocalDate.now();

        license.setUserId(userId);
        license.setFirstActivationDate(now);
        license.setEndingDate(now.plusDays(defaultDurationDays));

        License saved = licenseRepository.save(license);

        DeviceLicense dl = new DeviceLicense();
        dl.setLicenseId(saved.getId());
        dl.setDeviceId(device.getId());
        dl.setActivationDate(now);
        deviceLicenseRepository.save(dl);

        LicenseHistory history = new LicenseHistory();
        history.setLicenseId(saved.getId());
        history.setUserId(userId);
        history.setStatus("ACTIVATED");
        history.setChangeDate(LocalDateTime.now());
        history.setDescription(null);
        licenseHistoryRepository.save(history);

        return saved;
    }

    @Transactional
    public void repeatActivation(License license, UUID userId, Device device) {
        LocalDate now = LocalDate.now();

        DeviceLicense dl = new DeviceLicense();
        dl.setLicenseId(license.getId());
        dl.setDeviceId(device.getId());
        dl.setActivationDate(now);
        deviceLicenseRepository.save(dl);

        LicenseHistory history = new LicenseHistory();
        history.setLicenseId(license.getId());
        history.setUserId(userId);
        history.setStatus("ACTIVATED");
        history.setChangeDate(LocalDateTime.now());
        history.setDescription(null);
        licenseHistoryRepository.save(history);
    }
}