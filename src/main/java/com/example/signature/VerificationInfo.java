package com.example.signature;

public class VerificationInfo {

    private String keyAlias;
    private String certificateBase64;

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getCertificateBase64() {
        return certificateBase64;
    }

    public void setCertificateBase64(String certificateBase64) {
        this.certificateBase64 = certificateBase64;
    }
}