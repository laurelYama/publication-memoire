package com.esiitech.publication_memoire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MemoireNotFoundException extends RuntimeException {
    public MemoireNotFoundException(Long id) {
        super("Mémoire introuvable avec l'ID : " + id);
    }
}
