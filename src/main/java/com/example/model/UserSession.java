package com.example.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String deviceId;

    @Column(length = 512)
    private String accessToken;

    @Column(length = 512)
    private String refreshToken;

    private Instant accessTokenExpiry;
    private Instant refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Instant getAccessTokenExpiry() { return accessTokenExpiry; }
    public void setAccessTokenExpiry(Instant accessTokenExpiry) { this.accessTokenExpiry = accessTokenExpiry; }

    public Instant getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(Instant refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
}
