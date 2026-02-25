package com.example.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(unique = true)
    private String email;

    @Column(name = "is_account_expired", nullable = false)
    private boolean isAccountExpired;

    @Column(name = "is_account_locked", nullable = false)
    private boolean isAccountLocked;

    @Column(name = "is_credentials_expired", nullable = false)
    private boolean isCredentialsExpired;

    @Column(name = "is_disabled", nullable = false)
    private boolean isDisabled;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAccountExpired() { return isAccountExpired; }
    public void setAccountExpired(boolean accountExpired) { isAccountExpired = accountExpired; }

    public boolean isAccountLocked() { return isAccountLocked; }
    public void setAccountLocked(boolean accountLocked) { isAccountLocked = accountLocked; }

    public boolean isCredentialsExpired() { return isCredentialsExpired; }
    public void setCredentialsExpired(boolean credentialsExpired) { isCredentialsExpired = credentialsExpired; }

    public boolean isDisabled() { return isDisabled; }
    public void setDisabled(boolean disabled) { isDisabled = disabled; }
}