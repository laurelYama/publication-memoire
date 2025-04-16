package com.esiitech.publication_memoire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UtilisateurNotFoundException extends RuntimeException {
    public UtilisateurNotFoundException(Long id) {
        super("Utilisateur introuvable avec l'ID : " + id);
    }

    public UtilisateurNotFoundException(String email) {
        super("Utilisateur introuvable avec l'email : " + email);
    }
}
