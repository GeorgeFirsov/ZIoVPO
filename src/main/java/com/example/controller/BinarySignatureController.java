package com.example.controller;

import com.example.binary.BinaryPackage;
import com.example.dto.SignatureIdsRequest;
import com.example.service.BinarySignatureExportService;
import com.example.service.MultipartMixedResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/binary/signatures")
public class BinarySignatureController {

    private final BinarySignatureExportService binarySignatureExportService;
    private final MultipartMixedResponseFactory multipartMixedResponseFactory;

    public BinarySignatureController(BinarySignatureExportService binarySignatureExportService,
                                     MultipartMixedResponseFactory multipartMixedResponseFactory) {
        this.binarySignatureExportService = binarySignatureExportService;
        this.multipartMixedResponseFactory = multipartMixedResponseFactory;
    }

    @GetMapping("/full")
    public ResponseEntity<MultiValueMap<String, Object>> getFull() {
        BinaryPackage binaryPackage = binarySignatureExportService.buildFullExport();
        return multipartMixedResponseFactory.create(binaryPackage.getManifestBytes(), binaryPackage.getDataBytes());
    }

    @GetMapping("/increment")
    public ResponseEntity<MultiValueMap<String, Object>> getIncrement(@RequestParam Instant since) {
        BinaryPackage binaryPackage = binarySignatureExportService.buildIncrementExport(since);
        return multipartMixedResponseFactory.create(binaryPackage.getManifestBytes(), binaryPackage.getDataBytes());
    }

    @PostMapping("/by-ids")
    public ResponseEntity<MultiValueMap<String, Object>> getByIds(@RequestBody SignatureIdsRequest request) {
        BinaryPackage binaryPackage = binarySignatureExportService.buildByIdsExport(request);
        return multipartMixedResponseFactory.create(binaryPackage.getManifestBytes(), binaryPackage.getDataBytes());
    }
}