package com.esiitech.publication_memoire.dto;

import com.esiitech.publication_memoire.enums.Role;

public class UserDto {
    private Long id;
    private String nom;
    private String email;
    private Role role;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
