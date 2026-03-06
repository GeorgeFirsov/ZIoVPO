package com.example.signature;

public class SignatureModuleException extends RuntimeException {

    private final SignatureErrorCode code;

    public SignatureModuleException(SignatureErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public SignatureModuleException(SignatureErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public SignatureErrorCode getCode() {
        return code;
    }
}