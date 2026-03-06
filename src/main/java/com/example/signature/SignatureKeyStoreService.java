package com.example.signature;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

@Service
public class SignatureKeyStoreService {

    private final SignatureProperties properties;

    public SignatureKeyStoreService(SignatureProperties properties) {
        this.properties = properties;
    }

    public KeyStore loadKeyStore() {
        String type = properties.getKeyStoreType();
        String path = properties.getKeyStorePath();
        String storePassword = properties.getKeyStorePassword();

        if (type == null || type.isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "signature.key-store-type is empty");
        }
        if (path == null || path.isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "signature.key-store-path is empty");
        }
        if (storePassword == null) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "signature.key-store-password is null");
        }

        InputStream is = null;
        try {
            is = openStream(path);
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(is, storePassword.toCharArray());
            return keyStore;
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase().contains("password")) {
                throw new SignatureModuleException(SignatureErrorCode.AUTH_FAILED, "keystore password is incorrect", e);
            }
            throw new SignatureModuleException(SignatureErrorCode.KEY_SOURCE_UNAVAILABLE, "cannot open keystore: " + path, e);
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "cannot load keystore: " + path, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private InputStream openStream(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String p = path.substring("classpath:".length());
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            return new ClassPathResource(p).getInputStream();
        }

        if (path.startsWith("file:")) {
            String p = path.substring("file:".length());
            return new FileInputStream(p);
        }

        return new FileInputStream(path);
    }
}