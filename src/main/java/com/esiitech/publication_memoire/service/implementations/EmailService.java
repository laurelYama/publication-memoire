package com.esiitech.publication_memoire.service.implementations;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // Envoi d'un email d'activation avec rôle dynamique
    public void sendActivationEmail(String to, String activationToken, String nom, String prenom, String role) {
        String activationLink = "http://localhost:8080/api/utilisateurs/activer-compte/" + activationToken;

        String subject = "Activation de votre compte " + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

        String body = """
            Bonjour %s %s,

            Votre compte a été créé par l’administrateur avec le rôle : %s.

            Veuillez cliquer sur le lien suivant pour activer votre compte et définir votre mot de passe définitif :
            %s

            Ce lien est valide pendant 30 minutes.

            Cordialement,
            L'équipe de la plateforme.
            """.formatted(prenom, nom.toUpperCase(), role.toUpperCase(), activationLink);

        sendEmail(to, subject, body);
    }

}
