package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.config.JwtService;
import com.esiitech.publication_memoire.dto.ChangePasswordRequest;
import com.esiitech.publication_memoire.dto.ResetPasswordRequest;
import com.esiitech.publication_memoire.dto.UtilisateurDTO;
import com.esiitech.publication_memoire.entity.Utilisateur;
import com.esiitech.publication_memoire.enums.Role;
import com.esiitech.publication_memoire.logging.Loggable;
import com.esiitech.publication_memoire.mapper.UtilisateurMapper;
import com.esiitech.publication_memoire.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UtilisateurMapper utilisateurMapper;

    // Constructeur pour l'injection des dépendances
    public UtilisateurService(UtilisateurRepository utilisateurRepository, EmailService emailService, 
                              PasswordEncoder passwordEncoder, JwtService jwtService,
                              UtilisateurMapper utilisateurMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.utilisateurMapper = utilisateurMapper;

    }

    // Expression régulière pour valider la robustesse du mot de passe
    private static final String MOT_DE_PASSE_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    // Méthode pour vérifier si le mot de passe est suffisamment fort
    private boolean estMotDePasseFort(String motDePasse) {
        Pattern pattern = Pattern.compile(MOT_DE_PASSE_REGEX);
        Matcher matcher = pattern.matcher(motDePasse);
        return matcher.matches();  // Renvoie true si le mot de passe respecte les critères
    }

    // Génération d'un mot de passe temporaire aléatoire
    private String generateTemporaryPassword() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(charset.length());
            password.append(charset.charAt(randomIndex));
        }

        return password.toString();
    }

    /**
     * Crée un nouvel utilisateur avec un mot de passe temporaire et envoie un email de validation
     * @param nom Le nom de l'utilisateur
     * @param prenom Le prénom de l'utilisateur
     * @param email L'email de l'utilisateur
     * @param role Le rôle de l'utilisateur (ADMIN, ETUDIANT, etc.)
     * @return L'utilisateur créé
     */
    @Loggable
    public Utilisateur creerUtilisateur(String nom, String prenom, String email, Role role) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("L'email est déjà utilisé !");
        }

        String token = UUID.randomUUID().toString();
        String temporaryPassword = generateTemporaryPassword();

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setRole(role);
        utilisateur.setActivationToken(token);
        utilisateur.setMotDePasse(temporaryPassword);
        utilisateur.setTokenExpiration(LocalDateTime.now().plusHours(24));
        utilisateur.setActif(false);
        utilisateur.setPasswordCreated(false);

        utilisateurRepository.save(utilisateur);

        // Envoi d'un email d'activation
        emailService.sendActivationEmail(email, token, nom, prenom, role.name());

        return utilisateur;
    }

    /**
     * Permet à l'utilisateur de changer son mot de passe.
     * @param utilisateur L'utilisateur qui souhaite changer son mot de passe
     * @param request Contient les anciens et nouveaux mots de passe
     * @return Réponse HTTP en fonction du succès ou de l'échec de l'opération
     */
    @Loggable
    public ResponseEntity<?> changerMotDePasse(Utilisateur utilisateur, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            return ResponseEntity.badRequest().body("Ancien mot de passe incorrect.");
        }

        if (!request.getNouveauMotDePasse().equals(request.getConfirmationMotDePasse())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        if (!estMotDePasseFort(request.getNouveauMotDePasse())) {
            return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    /**
     * Envoie un lien de réinitialisation de mot de passe à l'utilisateur
     * @param email L'email de l'utilisateur qui a oublié son mot de passe
     * @return Réponse HTTP indiquant si l'email a bien été envoyé
     */
    @Loggable
    public ResponseEntity<?> envoyerLienDeReinitialisation(String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email non trouvé.");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        String token = UUID.randomUUID().toString();
        utilisateur.setActivationToken(token);
        utilisateur.setTokenExpiration(LocalDateTime.now().plusHours(1));
        utilisateurRepository.save(utilisateur);

        String lien = "http://localhost:8080/api/auth/reinitialiser-mot-de-passe/" + token;
        emailService.sendEmailAsync(utilisateur.getEmail(), "Réinitialisation de mot de passe", "Clique ici : " + lien);

        return ResponseEntity.ok("Email envoyé.");
    }

    /**
     * Permet à l'utilisateur de réinitialiser son mot de passe en utilisant un token d'activation
     * @param token Le token d'activation pour réinitialiser le mot de passe
     * @param request Contient le nouveau mot de passe et sa confirmation
     * @return Réponse HTTP indiquant si la réinitialisation a été effectuée avec succès
     */
    @Loggable
    public ResponseEntity<?> reinitialiserMotDePasse(String token, ResetPasswordRequest request) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByActivationToken(token);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Lien invalide.");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        if (utilisateur.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Lien expiré.");
        }

        if (!request.getNouveauMotDePasse().equals(request.getConfirmationMotDePasse())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        if (!estMotDePasseFort(request.getNouveauMotDePasse())) {
            return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setActivationToken(null);
        utilisateur.setTokenExpiration(null);
        utilisateur.setActif(true);
        utilisateur.setPasswordCreated(true);
        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Mot de passe réinitialisé.");
    }

    /**
     * Active ou désactive un utilisateur
     * @param id L'ID de l'utilisateur
     * @param actif Si true, l'utilisateur est activé, sinon il est désactivé
     * @return Réponse HTTP indiquant si l'activation a été effectuée avec succès
     */
    @Loggable
    public ResponseEntity<?> activerUtilisateur(Long id, boolean actif) {
        Optional<Utilisateur> opt = utilisateurRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = opt.get();
        utilisateur.setActif(actif);
        utilisateurRepository.save(utilisateur);

        String message = actif ? "Utilisateur activé" : "Utilisateur désactivé";
        return ResponseEntity.ok(Map.of("status", "success", "message", message));
    }

    /**
     * Récupère l'utilisateur en fonction du token JWT
     * @param token Le token JWT de l'utilisateur
     * @return L'utilisateur associé au token
     */
    public Utilisateur getUtilisateurDepuisToken(String token) {
        String email = jwtService.extraireEmail(token);
        return getByEmail(email);
    }

    /**
     * Récupère un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur correspondant à l'email
     */
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    /**
     * Récupère un utilisateur par son ID
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur correspondant à l'ID
     */
    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public List<UtilisateurDTO> recupererLecteurs() {
        List<Utilisateur> lecteurs = utilisateurRepository.findByRole(Role.LECTEUR);
        return utilisateurMapper.toDtoList(lecteurs);
    }

    public Utilisateur getUtilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Aucun utilisateur connecté.");
        }

        String email = authentication.getName(); // ou `getPrincipal()` si tu stockes un objet custom
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l’email : " + email));
    }


}
