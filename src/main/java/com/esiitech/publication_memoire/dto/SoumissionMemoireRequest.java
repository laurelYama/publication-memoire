package com.esiitech.publication_memoire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SoumissionMemoireRequest {

//    @NotNull
//    private Long etudiantId;

    @NotBlank
    private String titre;

    @NotBlank
    private String description;

    @NotNull
    private MultipartFile fichierWord;


//    public @NotNull Long getEtudiantId() {
//        return etudiantId;
//    }
//
//    public void setEtudiantId(@NotNull Long etudiantId) {
//        this.etudiantId = etudiantId;
//    }

    public @NotBlank String getTitre() {
        return titre;
    }

    public void setTitre(@NotBlank String titre) {
        this.titre = titre;
    }

    public @NotBlank String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank String description) {
        this.description = description;
    }

    public @NotNull MultipartFile getFichierWord() {
        return fichierWord;
    }

    public void setFichierWord(@NotNull MultipartFile fichierWord) {
        this.fichierWord = fichierWord;
    }
}

