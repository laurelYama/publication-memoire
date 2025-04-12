package com.esiitech.publication_memoire.enums;

public enum StatutMemoire {
    EN_ATTENTE,   // Soumis par l'étudiant
    REJETE,       // Rejeté par l’admin avec commentaire
    TRANSMIS,     // Transmis par le lecteur à l’admin
    VALIDE        // Validé par l’admin (avec conversion en PDF)
}

