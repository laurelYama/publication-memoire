package com.esiitech.publication_memoire.dto;

public class OtpRequestDto {
    private String email;
    private String otp;

    // Getters / Setters
    public String getEmail() {
        return email.trim(); // Nettoyage automatique
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp.trim();
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

