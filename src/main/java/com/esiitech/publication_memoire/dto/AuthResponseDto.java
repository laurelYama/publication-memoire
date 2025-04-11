package com.esiitech.publication_memoire.dto;

import com.esiitech.publication_memoire.enums.Role;

public class AuthResponseDto {
    private String token;
    private Long userId;
    private String nom;
    private String email;
    private Role role;

    // Getters & setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
