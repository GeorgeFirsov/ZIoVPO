package com.example.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class MultipartMixedResponseFactory {

    private static final MediaType MULTIPART_MIXED = MediaType.parseMediaType("multipart/mixed");

    public ResponseEntity<MultiValueMap<String, Object>> create(byte[] manifestBytes, byte[] dataBytes) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("manifest", createPart("manifest.bin", manifestBytes));
        body.add("data", createPart("data.bin", dataBytes));

        return ResponseEntity.ok()
                .contentType(MULTIPART_MIXED)
                .body(body);
    }

    private HttpEntity<ByteArrayResource> createPart(String fileName, byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        headers.setContentLength(bytes.length);
        return new HttpEntity<>(new NamedByteArrayResource(fileName, bytes), headers);
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String fileName;

        public NamedByteArrayResource(String fileName, byte[] byteArray) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}