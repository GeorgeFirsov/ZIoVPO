package com.example.service;

import com.example.config.MinioProperties;
import com.example.model.MalwareSignatureFile;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class SignatureFileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public SignatureFileStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public MalwareSignatureFile upload(MultipartFile file, UUID signatureId) {
        String originalFileName = cleanOriginalFileName(file.getOriginalFilename());
        String objectKey = buildObjectKey(signatureId, originalFileName);
        String contentType = resolveContentType(file);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot upload file to MinIO");
        }

        MalwareSignatureFile storedFile = new MalwareSignatureFile();
        storedFile.setSignatureId(signatureId);
        storedFile.setBucketName(minioProperties.getBucket());
        storedFile.setObjectKey(objectKey);
        storedFile.setOriginalFileName(originalFileName);
        storedFile.setContentType(contentType);
        storedFile.setFileSize(file.getSize());
        storedFile.setCreatedAt(Instant.now());
        return storedFile;
    }

    public String getPreSignedUrl(MalwareSignatureFile storedFile) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(storedFile.getBucketName())
                            .object(storedFile.getObjectKey())
                            .expiry(minioProperties.getUrlExpiryMinutes() * 60)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot build pre-signed URL");
        }
    }

    private String buildObjectKey(UUID signatureId, String originalFileName) {
        return "signatures/" + signatureId + "/" + sanitizeFileName(originalFileName);
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            return "application/octet-stream";
        }
        return contentType.trim();
    }

    private String cleanOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return "uploaded-file.bin";
        }
        String normalized = originalFileName.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        if (fileName.trim().isEmpty()) {
            return "uploaded-file.bin";
        }
        return fileName.trim();
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_").toLowerCase(Locale.ROOT);
    }
}