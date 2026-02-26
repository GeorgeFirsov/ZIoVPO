package com.example.service;

import com.example.model.Device;
import com.example.model.License;
import com.example.model.LicenseType;
import com.example.model.LicenseHistory;
import com.example.model.User;
import com.example.storage.DeviceLicenseRepository;
import com.example.storage.DeviceRepository;
import com.example.storage.LicenseHistoryRepository;
import com.example.storage.LicenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LicenseService {

    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final ApplicationUserService applicationUserService;
    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;

    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseActivationTransactionService txService;

    public LicenseService(ProductService productService,
                          LicenseTypeService licenseTypeService,
                          ApplicationUserService applicationUserService,
                          LicenseRepository licenseRepository,
                          LicenseHistoryRepository licenseHistoryRepository,
                          DeviceRepository deviceRepository,
                          DeviceLicenseRepository deviceLicenseRepository,
                          LicenseActivationTransactionService txService) {
        this.productService = productService;
        this.licenseTypeService = licenseTypeService;
        this.applicationUserService = applicationUserService;
        this.licenseRepository = licenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.txService = txService;
    }

    @Transactional
    public License createLicense(CreateLicenseRequest request, UUID adminId) {
        productService.getProductOrFail(request.getProductId());
        licenseTypeService.getTypeOrFail(request.getTypeId());
        User owner = applicationUserService.getActiveUserOrFail(request.getOwnerId());

        License license = createNewLicense(request.getProductId(), request.getTypeId(), owner.getId());

        License saved = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicenseId(saved.getId());
        history.setUserId(adminId);
        history.setStatus("CREATED");
        history.setChangeDate(LocalDateTime.now());
        history.setDescription(null);

        licenseHistoryRepository.save(history);

        return saved;
    }

    public Ticket activateLicense(ActivateLicenseRequest request, UUID userId) {
        License license = findByCodeOrFail(request.getActivationKey());

        if (license.getUserId() != null && !license.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "license owned by another user");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac());
        if (device == null) {
            device = new Device();
            device.setUserId(userId);
            device.setName(request.getDeviceName());
            device.setMacAddress(request.getDeviceMac());
            device = deviceRepository.save(device);
        }

        if (license.getUserId() == null) {
            LicenseType type = licenseTypeService.getTypeOrFail(license.getTypeId());
            License saved = txService.firstActivation(license, userId, type.getDefaultDurationInDays(), device);
            return buildTicket(saved);
        }

        long count = deviceLicenseRepository.countByLicenseId(license.getId());
        if (count >= license.getDeviceCount()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "device limit reached");
        }

        txService.repeatActivation(license, userId, device);
        return buildTicket(license);
    }

    private License findByCodeOrFail(String activationKey) {
        License license = licenseRepository.findByCode(activationKey);
        if (license == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "license not found");
        }
        return license;
    }

    private Ticket buildTicket(License license) {
        Ticket ticket = new Ticket();
        ticket.setLicenseId(license.getId());
        ticket.setCode(license.getCode());
        ticket.setUserId(license.getUserId());
        ticket.setOwnerId(license.getOwnerId());
        ticket.setProductId(license.getProductId());
        ticket.setTypeId(license.getTypeId());
        ticket.setFirstActivationDate(license.getFirstActivationDate());
        ticket.setEndingDate(license.getEndingDate());
        ticket.setDeviceCount(license.getDeviceCount());
        return ticket;
    }

    private License createNewLicense(UUID productId, UUID typeId, UUID ownerId) {
        License license = new License();
        license.setCode(generateCode());
        license.setProductId(productId);
        license.setTypeId(typeId);
        license.setOwnerId(ownerId);
        license.setUserId(null);
        license.setBlocked(false);
        license.setDeviceCount(1);
        license.setFirstActivationDate(null);
        license.setEndingDate(null);
        license.setDescription(null);
        return license;
    }

    private String generateCode() {
        String code = null;
        int tries = 0;

        while (tries < 10) {
            code = UUID.randomUUID().toString().replace("-", "");
            if (!licenseRepository.existsByCode(code)) {
                return code;
            }
            tries++;
        }

        return UUID.randomUUID().toString().replace("-", "");
    }

    public static class CreateLicenseRequest {
        private UUID productId;
        private UUID typeId;
        private UUID ownerId;

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public UUID getTypeId() { return typeId; }
        public void setTypeId(UUID typeId) { this.typeId = typeId; }

        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    }

    public static class ActivateLicenseRequest {
        private String activationKey;
        private String deviceMac;
        private String deviceName;

        public String getActivationKey() { return activationKey; }
        public void setActivationKey(String activationKey) { this.activationKey = activationKey; }

        public String getDeviceMac() { return deviceMac; }
        public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    }

    public static class Ticket {
        private UUID licenseId;
        private String code;
        private UUID userId;
        private UUID ownerId;
        private UUID productId;
        private UUID typeId;
        private java.time.LocalDate firstActivationDate;
        private java.time.LocalDate endingDate;
        private int deviceCount;

        public UUID getLicenseId() { return licenseId; }
        public void setLicenseId(UUID licenseId) { this.licenseId = licenseId; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public UUID getTypeId() { return typeId; }
        public void setTypeId(UUID typeId) { this.typeId = typeId; }

        public java.time.LocalDate getFirstActivationDate() { return firstActivationDate; }
        public void setFirstActivationDate(java.time.LocalDate firstActivationDate) { this.firstActivationDate = firstActivationDate; }

        public java.time.LocalDate getEndingDate() { return endingDate; }
        public void setEndingDate(java.time.LocalDate endingDate) { this.endingDate = endingDate; }

        public int getDeviceCount() { return deviceCount; }
        public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    }
}