package com.esiitech.publication_memoire.service;

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

    // Envoi d'un email d'activation sans mot de passe temporaire
    public void sendActivationEmail(String to, String activationToken) {
        String activationLink = "http://localhost:8080/api/utilisateurs/activer-compte/" + activationToken;

        String subject = "Activation de votre compte Lecteur";
        String body = """
                Bonjour,

                Votre compte a été créé par l’administrateur.

                Veuillez cliquer sur le lien suivant pour activer votre compte et définir votre mot de passe définitif :
                %s

                Ce lien est valide pendant 30 minutes.

                Cordialement,
                L'équipe de la plateforme.
                """.formatted(activationLink);

        sendEmail(to, subject, body);
    }
}
