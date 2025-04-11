package com.esiitech.publication_memoire.dto;

public class AuthRequestDto {
    private String email;
    private String motDePasse;

    // Getters & setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
