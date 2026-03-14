package com.example.controller;

import com.example.model.License;
import com.example.model.User;
import com.example.service.ApplicationUserService;
import com.example.service.LicenseService;
import com.example.signature.TicketSigningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LicenseController {

    private final LicenseService licenseService;
    private final ApplicationUserService applicationUserService;
    private final TicketSigningService ticketSigningService;

    public LicenseController(LicenseService licenseService,
                             ApplicationUserService applicationUserService,
                             TicketSigningService ticketSigningService) {
        this.licenseService = licenseService;
        this.applicationUserService = applicationUserService;
        this.ticketSigningService = ticketSigningService;
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
    public ResponseEntity<LicenseService.Ticket> activateLicense(@RequestBody LicenseService.ActivateLicenseRequest request,
                                                                 Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = auth.getName();
        User user = applicationUserService.getUserByUsernameOrFail(username);

        LicenseService.Ticket ticket = licenseService.activateLicense(request, user.getId());
        ticketSigningService.signTicket(ticket);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/api/licenses/renew")
    public ResponseEntity<LicenseService.Ticket> renewLicense(@RequestBody LicenseService.RenewLicenseRequest request,
                                                              Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = auth.getName();
        User user = applicationUserService.getUserByUsernameOrFail(username);

        LicenseService.Ticket ticket = licenseService.renewLicense(request, user.getId());
        ticketSigningService.signTicket(ticket);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/api/licenses/check")
    public ResponseEntity<LicenseService.Ticket> checkLicense(@RequestBody LicenseService.CheckLicenseRequest request,
                                                              Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = auth.getName();
        User user = applicationUserService.getUserByUsernameOrFail(username);

        LicenseService.Ticket ticket = licenseService.checkLicense(request, user.getId());
        ticketSigningService.signTicket(ticket);
        return ResponseEntity.ok(ticket);
    }
}