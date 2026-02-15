package com.example.controller;

import com.example.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenService.TokenResponse> login(@RequestBody LoginRequest request) {
        String deviceId = request.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "unknown";
        }
        TokenService.TokenResponse tokens =
                tokenService.login(request.getUsername(), request.getPassword(), deviceId);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenService.TokenResponse> refresh(@RequestBody RefreshRequest request) {
        TokenService.TokenResponse tokens = tokenService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    public static class LoginRequest {
        private String username;
        private String password;
        private String deviceId;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    }

    public static class RefreshRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}
