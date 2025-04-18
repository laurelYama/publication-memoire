package com.esiitech.publication_memoire.controller;

import com.esiitech.publication_memoire.dto.OtpRequestDto;
import com.esiitech.publication_memoire.dto.PasswordRequestDto;
import com.esiitech.publication_memoire.service.EmailService;
import com.esiitech.publication_memoire.service.OtpService;
import com.esiitech.publication_memoire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@Tag(name = "OTP Controller", description = "Gestion des OTP et de la création de mot de passe utilisateur")
public class OtpController {

    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurService utilisateurService;

    public OtpController(OtpService otpService, EmailService emailService,
                         PasswordEncoder passwordEncoder, UtilisateurService utilisateurService) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurService = utilisateurService;
    }

    public boolean motDePasseEstFort(String motDePasse) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d[^\\s]]{8,}$";
        return motDePasse.matches(regex);
    }

    @Operation(
            summary = "Envoyer un OTP à un utilisateur",
            description = "Génère un OTP aléatoire, l’enregistre côté serveur et l’envoie à l’adresse e-mail fournie.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP envoyé avec succès"),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la requête")
            }
    )
    @PostMapping("/envoyer-otp")
    public ResponseEntity<String> envoyerOtp(
            @RequestBody @Parameter(description = "Corps contenant l'e-mail de l'utilisateur", required = true)
            Map<String, String> body) {

        String email = body.get("email");
        String otp = otpService.genererOtp();
        otpService.sauvegarderOtp(email, otp);
        emailService.envoyerOtp(email, otp);

        return ResponseEntity.ok("OTP envoyé !");
    }

    @Operation(
            summary = "Valider un OTP",
            description = "Vérifie si l’OTP fourni pour un e-mail donné est valide et non expiré.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP validé avec succès"),
                    @ApiResponse(responseCode = "400", description = "OTP invalide ou expiré")
            }
    )
    @PostMapping("/valider")
    public ResponseEntity<String> validerOtp(
            @RequestBody @Parameter(description = "Objet contenant l’e-mail et l’OTP saisi par l’utilisateur", required = true)
            OtpRequestDto dto) {

        if (dto.getEmail() == null || dto.getOtp() == null) {
            return ResponseEntity.badRequest().body("Email et OTP sont requis.");
        }

        boolean estValide = otpService.validerOtp(dto);
        if (estValide) {
            return ResponseEntity.ok("OTP validé avec succès !");
        } else {
            return ResponseEntity.badRequest().body("OTP invalide ou expiré.");
        }
    }

    @Operation(
            summary = "Définir un mot de passe après validation de l'OTP",
            description = "Permet à un utilisateur de définir un mot de passe après validation d’un OTP reçu par email.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mot de passe défini et compte créé"),
                    @ApiResponse(responseCode = "400", description = "Erreur de validation (champs manquants, mot de passe faible, etc.)")
            }
    )
    @PostMapping("/definir-mot-de-passe")
    public ResponseEntity<String> definirMotDePasse(
            @RequestBody @Parameter(description = "Objet contenant l’e-mail, le mot de passe et sa confirmation", required = true)
            PasswordRequestDto dto) {

        if (dto.getEmail() == null || dto.getMotDePasse() == null || dto.getConfirmation() == null) {
            return ResponseEntity.badRequest().body("Tous les champs sont requis.");
        }

        if (!dto.getMotDePasse().equals(dto.getConfirmation())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        if (!motDePasseEstFort(dto.getMotDePasse())) {
            return ResponseEntity.badRequest().body("Le mot de passe doit être fort.");
        }

        try {
            utilisateurService.creerUtilisateurDepuisTrombinoscope(dto.getEmail(), dto.getMotDePasse());
            return ResponseEntity.ok("Compte créé avec succès !");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
