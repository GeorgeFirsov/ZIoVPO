package com.example.binary;

public class BinaryPackage {

    private final byte[] manifestBytes;
    private final byte[] dataBytes;

    public BinaryPackage(byte[] manifestBytes, byte[] dataBytes) {
        this.manifestBytes = manifestBytes;
        this.dataBytes = dataBytes;
    }

    public byte[] getManifestBytes() {
        return manifestBytes;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }
}