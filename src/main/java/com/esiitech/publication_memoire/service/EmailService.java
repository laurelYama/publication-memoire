package com.esiitech.publication_memoire.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmailAsync(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
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
            Esiitech-Archives.
            """.formatted(prenom, nom.toUpperCase(), role.toUpperCase(), activationLink);

        sendEmailAsync(to, subject, body);
    }

    public void envoyerOtp(String to, String otp) {
        String sujet = "Votre code OTP de vérification";
        String corps = "Bonjour,\n\nVotre code de confirmation est : " + otp +
                "\nIl est valide pendant 5 minutes." +
                "\n\nSi vous n'avez pas demandé ce code, veuillez ignorer ce message." +
                "\n\nCordialement,\nEsiitech-Archives";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(sujet);
            helper.setText(corps, false); // false = plain text

            mailSender.send(message);
            System.out.println("Mail OTP envoyé à " + to);

        } catch (Exception e) {
            System.err.println("Erreur d'envoi OTP à " + to + " : " + e.getMessage());
        }
    }





}
