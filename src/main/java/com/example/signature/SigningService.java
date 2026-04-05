package com.example.signature;

public interface SigningService {
    String sign(Object payload);

    byte[] signBytes(byte[] payload);
}