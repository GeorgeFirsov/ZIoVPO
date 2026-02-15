package com.example.service;

import com.example.model.SessionStatus;
import com.example.model.User;
import com.example.model.UserSession;
import com.example.security.JwtTokenProvider;
import com.example.storage.UserRepository;
import com.example.storage.UserSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenService(UserRepository userRepository,
                        UserSessionRepository userSessionRepository,
                        JwtTokenProvider jwtTokenProvider,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenResponse login(String username, String password, String deviceId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String role = user.getRole();
        String accessToken = jwtTokenProvider.createAccessToken(username, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(username, deviceId);

        Instant now = Instant.now();

        UserSession session = new UserSession();
        session.setUsername(username);
        session.setDeviceId(deviceId);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setAccessTokenExpiry(now.plusMillis(jwtTokenProvider.getAccessTokenValidityMs()));
        session.setRefreshTokenExpiry(now.plusMillis(jwtTokenProvider.getRefreshTokenValidityMs()));
        session.setStatus(SessionStatus.ACTIVE);

        userSessionRepository.save(session);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Optional<UserSession> optionalSession = userSessionRepository.findByRefreshToken(refreshToken);
        if (optionalSession.isEmpty()) {
            throw new RuntimeException("Session not found");
        }

        UserSession session = optionalSession.get();

        if (session.getStatus() == SessionStatus.USED) {
            session.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token already used");
        }

        if (session.getStatus() == SessionStatus.REVOKED) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (session.getRefreshTokenExpiry().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        String username = session.getUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String role = user.getRole();

        String newAccessToken = jwtTokenProvider.createAccessToken(username, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username, session.getDeviceId());

        Instant now = Instant.now();

        // старый refresh делаем USED
        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        // новая сессия
        UserSession newSession = new UserSession();
        newSession.setUsername(username);
        newSession.setDeviceId(session.getDeviceId());
        newSession.setAccessToken(newAccessToken);
        newSession.setRefreshToken(newRefreshToken);
        newSession.setAccessTokenExpiry(now.plusMillis(jwtTokenProvider.getAccessTokenValidityMs()));
        newSession.setRefreshTokenExpiry(now.plusMillis(jwtTokenProvider.getRefreshTokenValidityMs()));
        newSession.setStatus(SessionStatus.ACTIVE);

        userSessionRepository.save(newSession);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;

        public TokenResponse() {
        }

        public TokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}
