package com.esiitech.publication_memoire.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TransmissionLecteurRequest {

    @NotNull
    private Long memoireId;

    private String commentaire;

    @NotNull
    private MultipartFile fichierCorrige;


    public @NotNull Long getMemoireId() {
        return memoireId;
    }

    public void setMemoireId(@NotNull Long memoireId) {
        this.memoireId = memoireId;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public @NotNull MultipartFile getFichierCorrige() {
        return fichierCorrige;
    }

    public void setFichierCorrige(@NotNull MultipartFile fichierCorrige) {
        this.fichierCorrige = fichierCorrige;
    }
}

