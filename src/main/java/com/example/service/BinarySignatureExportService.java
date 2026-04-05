package com.example.service;

import com.example.binary.BinaryExportType;
import com.example.binary.BinaryOutput;
import com.example.binary.BinaryPackage;
import com.example.dto.SignatureIdsRequest;
import com.example.model.MalwareSignature;
import com.example.model.SignatureStatus;
import com.example.signature.SignatureErrorCode;
import com.example.signature.SignatureModuleException;
import com.example.signature.SigningService;
import com.example.storage.MalwareSignatureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BinarySignatureExportService {

    private static final byte[] MANIFEST_MAGIC = new byte[]{'M', 'F', '-', 'F'};
    private static final byte[] DATA_MAGIC = new byte[]{'D', 'B', '-', 'F'};
    private static final int FORMAT_VERSION = 1;
    private static final long NO_SINCE_VALUE = -1L;

    private final MalwareSignatureRepository malwareSignatureRepository;
    private final SigningService signingService;

    public BinarySignatureExportService(MalwareSignatureRepository malwareSignatureRepository,
                                        SigningService signingService) {
        this.malwareSignatureRepository = malwareSignatureRepository;
        this.signingService = signingService;
    }

    public BinaryPackage buildFullExport() {
        List<MalwareSignature> signatures = malwareSignatureRepository.findByStatusOrderByUpdatedAtDesc(SignatureStatus.ACTUAL);
        return buildPackage(signatures, BinaryExportType.FULL, NO_SINCE_VALUE);
    }

    public BinaryPackage buildIncrementExport(Instant since) {
        if (since == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "since is required");
        }

        List<MalwareSignature> signatures = malwareSignatureRepository.findByUpdatedAtAfterOrderByUpdatedAtAsc(since);
        return buildPackage(signatures, BinaryExportType.INCREMENT, since.toEpochMilli());
    }

    public BinaryPackage buildByIdsExport(SignatureIdsRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ids are required");
        }

        List<MalwareSignature> found = malwareSignatureRepository.findByIdIn(request.getIds());
        return buildPackage(orderByRequestIds(found, request.getIds()), BinaryExportType.BY_IDS, NO_SINCE_VALUE);
    }

    private BinaryPackage buildPackage(List<MalwareSignature> signatures,
                                       BinaryExportType exportType,
                                       long sinceEpochMillis) {
        List<DataRecord> dataRecords = buildDataRecords(signatures);
        byte[] dataBytes = buildDataBytes(dataRecords);
        byte[] dataSha256 = sha256(dataBytes);
        byte[] unsignedManifestBytes = buildUnsignedManifestBytes(signatures, dataRecords, exportType, sinceEpochMillis, dataSha256);
        byte[] manifestSignatureBytes = signManifest(unsignedManifestBytes);
        byte[] manifestBytes = buildSignedManifestBytes(unsignedManifestBytes, manifestSignatureBytes);
        return new BinaryPackage(manifestBytes, dataBytes);
    }

    private List<MalwareSignature> orderByRequestIds(List<MalwareSignature> found, List<UUID> ids) {
        Map<UUID, MalwareSignature> byId = new HashMap<>();
        for (MalwareSignature item : found) {
            byId.put(item.getId(), item);
        }

        List<MalwareSignature> ordered = new ArrayList<>();
        List<UUID> processedIds = new ArrayList<>();
        for (UUID id : ids) {
            if (processedIds.contains(id)) {
                continue;
            }

            MalwareSignature item = byId.get(id);
            if (item != null) {
                ordered.add(item);
                processedIds.add(id);
            }
        }
        return ordered;
    }

    private List<DataRecord> buildDataRecords(List<MalwareSignature> signatures) {
        List<DataRecord> dataRecords = new ArrayList<>();
        long offset = 0L;

        for (MalwareSignature signature : signatures) {
            byte[] recordBytes = buildSingleDataRecord(signature);
            DataRecord dataRecord = new DataRecord();
            dataRecord.setOffset(offset);
            dataRecord.setLength(recordBytes.length);
            dataRecord.setBytes(recordBytes);
            dataRecords.add(dataRecord);
            offset += recordBytes.length;
        }

        return dataRecords;
    }

    private byte[] buildSingleDataRecord(MalwareSignature signature) {
        BinaryOutput output = new BinaryOutput();
        output.writeUtf8(signature.getThreatName());
        output.writeLengthPrefixedBytes(decodeHex(signature.getFirstBytesHex()));
        output.writeLengthPrefixedBytes(decodeHex(signature.getRemainderHashHex()));
        output.writeI64(signature.getRemainderLength());
        output.writeUtf8(signature.getFileType());
        output.writeI64(signature.getOffsetStart());
        output.writeI64(signature.getOffsetEnd());
        return output.toByteArray();
    }

    private byte[] buildDataBytes(List<DataRecord> dataRecords) {
        BinaryOutput output = new BinaryOutput();
        output.writeMagic(DATA_MAGIC);
        output.writeU16(FORMAT_VERSION);
        output.writeU32(dataRecords.size());

        for (DataRecord dataRecord : dataRecords) {
            output.writeRawBytes(dataRecord.getBytes());
        }

        return output.toByteArray();
    }

    private byte[] buildUnsignedManifestBytes(List<MalwareSignature> signatures,
                                              List<DataRecord> dataRecords,
                                              BinaryExportType exportType,
                                              long sinceEpochMillis,
                                              byte[] dataSha256) {
        if (dataSha256.length != 32) {
            throw new IllegalArgumentException("dataSha256 must contain 32 bytes");
        }

        BinaryOutput output = new BinaryOutput();
        output.writeMagic(MANIFEST_MAGIC);
        output.writeU16(FORMAT_VERSION);
        output.writeU8(exportType.getCode());
        output.writeI64(Instant.now().toEpochMilli());
        output.writeI64(sinceEpochMillis);
        output.writeU32(signatures.size());
        output.writeRawBytes(dataSha256);

        for (int i = 0; i < signatures.size(); i++) {
            MalwareSignature signature = signatures.get(i);
            DataRecord dataRecord = dataRecords.get(i);
            byte[] recordSignatureBytes = decodeBase64(signature.getDigitalSignatureBase64());

            output.writeUuid(signature.getId());
            output.writeU8(toStatusCode(signature.getStatus()));
            output.writeI64(signature.getUpdatedAt().toEpochMilli());
            output.writeU64(dataRecord.getOffset());
            output.writeU32(dataRecord.getLength());
            output.writeU32(recordSignatureBytes.length);
            output.writeRawBytes(recordSignatureBytes);
        }

        return output.toByteArray();
    }

    private byte[] buildSignedManifestBytes(byte[] unsignedManifestBytes, byte[] manifestSignatureBytes) {
        BinaryOutput output = new BinaryOutput();
        output.writeRawBytes(unsignedManifestBytes);
        output.writeU32(manifestSignatureBytes.length);
        output.writeRawBytes(manifestSignatureBytes);
        return output.toByteArray();
    }

    private byte[] signManifest(byte[] manifestBytes) {
        try {
            return signingService.signBytes(manifestBytes);
        } catch (SignatureModuleException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "signature error: " + SignatureErrorCode.SIGN_OPERATION_FAILED
            );
        }
    }

    private byte[] sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (Exception e) {
            throw new IllegalStateException("cannot calculate sha-256", e);
        }
    }

    private byte[] decodeBase64(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid signature base64");
        }
    }

    private byte[] decodeHex(String value) {
        if (value == null || value.length() % 2 != 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid hex value");
        }

        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            int high = Character.digit(value.charAt(i), 16);
            int low = Character.digit(value.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "invalid hex value");
            }
            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        return bytes;
    }

    private int toStatusCode(SignatureStatus status) {
        if (status == SignatureStatus.ACTUAL) {
            return 1;
        }
        return 2;
    }

    private static class DataRecord {
        private long offset;
        private int length;
        private byte[] bytes;

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }
}