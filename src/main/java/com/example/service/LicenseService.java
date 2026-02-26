package com.example.service;

import com.example.model.License;
import com.example.model.LicenseHistory;
import com.example.model.User;
import com.example.storage.LicenseHistoryRepository;
import com.example.storage.LicenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LicenseService {

    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final ApplicationUserService applicationUserService;
    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;

    public LicenseService(ProductService productService,
                          LicenseTypeService licenseTypeService,
                          ApplicationUserService applicationUserService,
                          LicenseRepository licenseRepository,
                          LicenseHistoryRepository licenseHistoryRepository) {
        this.productService = productService;
        this.licenseTypeService = licenseTypeService;
        this.applicationUserService = applicationUserService;
        this.licenseRepository = licenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
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
}