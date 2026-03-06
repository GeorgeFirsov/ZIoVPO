package com.example.signature;

public interface KeyProvider {
    SigningKey getSigningKey();
    VerificationInfo getVerificationInfo();
}