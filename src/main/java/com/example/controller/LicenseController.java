package com.example.controller;

import com.example.model.License;
import com.example.model.User;
import com.example.service.ApplicationUserService;
import com.example.service.LicenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LicenseController {

    private final LicenseService licenseService;
    private final ApplicationUserService applicationUserService;

    public LicenseController(LicenseService licenseService, ApplicationUserService applicationUserService) {
        this.licenseService = licenseService;
        this.applicationUserService = applicationUserService;
    }

    @PostMapping("/api/admin/licenses")
    public ResponseEntity<License> createLicense(@RequestBody LicenseService.CreateLicenseRequest request,
                                                 Authentication auth) {
        String username = auth.getName();
        User admin = applicationUserService.getUserByUsernameOrFail(username);

        License created = licenseService.createLicense(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/api/licenses/activate")
    public ResponseEntity<LicenseService.RenewTicket> activateLicense(@RequestBody LicenseService.ActivateLicenseRequest request,
                                                                      Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = auth.getName();
        User user = applicationUserService.getUserByUsernameOrFail(username);

        LicenseService.RenewTicket ticket = licenseService.activateLicense(request, user.getId());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/api/licenses/renew")
    public ResponseEntity<LicenseService.RenewTicket> renewLicense(@RequestBody LicenseService.RenewLicenseRequest request,
                                                                   Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = auth.getName();
        User user = applicationUserService.getUserByUsernameOrFail(username);

        LicenseService.RenewTicket ticket = licenseService.renewLicense(request, user.getId());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/api/licenses/check")
    public ResponseEntity<LicenseService.CheckTicket> checkLicense(@RequestBody LicenseService.CheckLicenseRequest request,
                                                                   Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = auth.getName();
        User user = applicationUserService.getUserByUsernameOrFail(username);

        LicenseService.CheckTicket ticket = licenseService.checkLicense(request, user.getId());
        return ResponseEntity.ok(ticket);
    }
}