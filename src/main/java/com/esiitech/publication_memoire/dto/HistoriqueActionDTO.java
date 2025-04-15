package com.esiitech.publication_memoire.dto;

import com.esiitech.publication_memoire.entity.HistoriqueAction;

public class HistoriqueActionDTO {
    private String action;
    private String dateAction;
    private String nomMemoire;

    public HistoriqueActionDTO(HistoriqueAction action) {
        this.action = action.getAction();
        this.dateAction = action.getDateAction().toString();
        this.nomMemoire = action.getMemoire() != null ? action.getMemoire().getTitre() : null;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDateAction() {
        return dateAction;
    }

    public void setDateAction(String dateAction) {
        this.dateAction = dateAction;
    }

    public String getNomMemoire() {
        return nomMemoire;
    }

    public void setNomMemoire(String nomMemoire) {
        this.nomMemoire = nomMemoire;
    }
}

