package com.esiitech.publication_memoire.dto;

import java.time.LocalDateTime;

public class OtpEntry {
    private String otp;
    private LocalDateTime expiration;

    public OtpEntry(String otp, LocalDateTime expiration) {
        this.otp = otp;
        this.expiration = expiration;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }
}

