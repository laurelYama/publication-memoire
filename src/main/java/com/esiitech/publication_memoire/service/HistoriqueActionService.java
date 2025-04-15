package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.entity.HistoriqueAction;
import com.esiitech.publication_memoire.entity.Memoire;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.logging.Loggable;
import com.esiitech.publication_memoire.repository.HistoriqueActionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour la gestion de l'historique des actions effectuées par les utilisateurs
 * sur les mémoires (consultation, modification, suppression, etc.).
 */
@Service
public class HistoriqueActionService {

    private final HistoriqueActionRepository historiqueActionRepository;

    /**
     * Constructeur avec injection du repository.
     *
     * @param historiqueActionRepository le repository pour l'entité HistoriqueAction
     */
    public HistoriqueActionService(HistoriqueActionRepository historiqueActionRepository) {
        this.historiqueActionRepository = historiqueActionRepository;
    }

    /**
     * Enregistre une action effectuée par un utilisateur sur un mémoire.
     *
     * @param utilisateur l'utilisateur qui a effectué l'action
     * @param action      la description de l'action (ex: "création", "modification", "suppression")
     * @param memoire     le mémoire concerné par l'action
     */
    public void enregistrerAction(Utilisateur utilisateur, String action, Memoire memoire) {
        HistoriqueAction historique = new HistoriqueAction();
        historique.setUtilisateur(utilisateur);
        historique.setAction(action);
        historique.setDateAction(LocalDateTime.now());
        historique.setMemoire(memoire);

        historiqueActionRepository.save(historique);
    }

    /**
     * Récupère la liste des actions effectuées par un utilisateur donné.
     * La méthode est annotée avec @Loggable pour journaliser les appels.
     *
     * @param utilisateur l'utilisateur dont on souhaite consulter l'historique
     * @return la liste des actions effectuées par cet utilisateur
     */
    @Loggable
    public List<HistoriqueAction> getHistoriqueParUtilisateur(Utilisateur utilisateur) {
        return historiqueActionRepository.findByUtilisateur(utilisateur);
    }
}
