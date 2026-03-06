package com.example.signature;

public interface CanonicalizationService {
    CanonicalBytes canonicalize(Object payload);
}