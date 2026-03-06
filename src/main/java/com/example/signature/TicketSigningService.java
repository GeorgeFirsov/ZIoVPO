package com.example.signature;

import com.example.service.LicenseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class TicketSigningService {

    private final SigningService signingService;
    private final ObjectMapper objectMapper;

    public TicketSigningService(SigningService signingService, ObjectMapper objectMapper) {
        this.signingService = signingService;
        this.objectMapper = objectMapper;
    }

    public void signRenewTicket(LicenseService.RenewTicket ticket) {
        try {
            String sig = signWithoutSignatureField(ticket);
            ticket.setSignature(sig);
        } catch (SignatureModuleException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "signature error: " + e.getCode());
        }
    }

    public void signCheckTicket(LicenseService.CheckTicket ticket) {
        try {
            String sig = signWithoutSignatureField(ticket);
            ticket.setSignature(sig);
        } catch (SignatureModuleException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "signature error: " + e.getCode());
        }
    }

    private String signWithoutSignatureField(Object ticket) {
        Map<String, Object> map = objectMapper.convertValue(ticket, new TypeReference<Map<String, Object>>() {
        });
        map.remove("signature");
        return signingService.sign(map);
    }
}