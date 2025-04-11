package com.esiitech.publication_memoire.dto;

import com.esiitech.publication_memoire.enums.Role;

public class RegisterRequestDto {
    private String nom;
    private String email;
    private String motDePasse;
    private Role role;

    // Getters & setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
