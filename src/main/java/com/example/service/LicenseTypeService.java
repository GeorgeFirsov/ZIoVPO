package com.example.service;

import com.example.model.LicenseType;
import com.example.storage.LicenseTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    public LicenseType getTypeOrFail(UUID typeId) {
        LicenseType type = licenseTypeRepository.findById(typeId).orElse(null);
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return type;
    }
}