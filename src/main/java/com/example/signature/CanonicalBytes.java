package com.example.signature;

public class CanonicalBytes {

    private final byte[] bytes;

    public CanonicalBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}