package com.example.signature;

import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Base64;

@Service
public class KeyStoreKeyProvider implements KeyProvider {

    private final SignatureProperties properties;
    private final SignatureKeyStoreService keyStoreService;

    private SigningKey cachedSigningKey;
    private VerificationInfo cachedVerificationInfo;

    public KeyStoreKeyProvider(SignatureProperties properties, SignatureKeyStoreService keyStoreService) {
        this.properties = properties;
        this.keyStoreService = keyStoreService;
    }

    @Override
    public SigningKey getSigningKey() {
        ensureLoaded();
        return cachedSigningKey;
    }

    @Override
    public VerificationInfo getVerificationInfo() {
        ensureLoaded();
        return cachedVerificationInfo;
    }

    private void ensureLoaded() {
        if (cachedSigningKey != null && cachedVerificationInfo != null) {
            return;
        }

        synchronized (this) {
            if (cachedSigningKey != null && cachedVerificationInfo != null) {
                return;
            }
            loadFromKeyStore();
        }
    }

    private void loadFromKeyStore() {
        String alias = properties.getKeyAlias();
        String keyPassword = properties.getKeyPassword();

        if (alias == null || alias.isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "signature.key-alias is empty");
        }

        if (keyPassword == null || keyPassword.isBlank()) {
            keyPassword = properties.getKeyStorePassword();
        }

        KeyStore ks = keyStoreService.loadKeyStore();

        try {
            Key key = ks.getKey(alias, keyPassword.toCharArray());
            if (key == null) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "key not found by alias: " + alias);
            }
            if (!(key instanceof PrivateKey)) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "key is not PrivateKey: " + alias);
            }

            Certificate cert = ks.getCertificate(alias);
            if (cert == null) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "certificate not found by alias: " + alias);
            }

            cachedSigningKey = new SigningKey((PrivateKey) key);

            VerificationInfo info = new VerificationInfo();
            info.setKeyAlias(alias);
            info.setCertificateBase64(Base64.getEncoder().encodeToString(cert.getEncoded()));
            cachedVerificationInfo = info;
        } catch (UnrecoverableKeyException e) {
            throw new SignatureModuleException(SignatureErrorCode.AUTH_FAILED, "key password is incorrect", e);
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "cannot read key/cert from keystore", e);
        }
    }
}