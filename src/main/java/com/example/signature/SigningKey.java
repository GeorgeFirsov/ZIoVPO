package com.example.signature;

import java.security.PrivateKey;

public class SigningKey {

    private final PrivateKey privateKey;

    public SigningKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}