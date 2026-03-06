package com.example.signature;

import org.springframework.stereotype.Service;

import java.security.Signature;
import java.util.Base64;

@Service
public class Sha256RsaSigningService implements SigningService {

    private final CanonicalizationService canonicalizationService;
    private final KeyProvider keyProvider;

    public Sha256RsaSigningService(CanonicalizationService canonicalizationService, KeyProvider keyProvider) {
        this.canonicalizationService = canonicalizationService;
        this.keyProvider = keyProvider;
    }

    @Override
    public String sign(Object payload) {
        CanonicalBytes canonicalBytes = canonicalizationService.canonicalize(payload);

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyProvider.getSigningKey().getPrivateKey());
            signature.update(canonicalBytes.getBytes());
            byte[] sig = signature.sign();
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.SIGN_OPERATION_FAILED, "sign operation failed", e);
        }
    }
}