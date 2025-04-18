package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.dto.OtpEntry;
import com.esiitech.publication_memoire.dto.OtpRequestDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // Stockage temporaire en mémoire, comme un casier
    private final ConcurrentHashMap<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();

    // Génère un OTP à 6 chiffres
    public String genererOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    // Enregistre le OTP avec expiration à +5 minutes
    public void sauvegarderOtp(String email, String otp) {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);
        otpStorage.put(email.trim(), new OtpEntry(otp.trim(), expiration));
    }



    public boolean validerOtp(OtpRequestDto dto) {
        OtpEntry entry = otpStorage.get(dto.getEmail());

        if (entry == null) return false;

        boolean estValide = entry.getOtp().equals(dto.getOtp()) &&
                LocalDateTime.now().isBefore(entry.getExpiration());

        if (estValide) {
            otpStorage.remove(dto.getEmail());  // Suppression du OTP valide
        }

        return estValide;
    }


    // Nettoyage automatique des OTP expirés toutes les 30 secondes
    @Scheduled(fixedRate = 30000)
    public void nettoyerOtpsExpirés() {
        LocalDateTime maintenant = LocalDateTime.now();
        Iterator<Map.Entry<String, OtpEntry>> it = otpStorage.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, OtpEntry> entry = it.next();
            if (entry.getValue().getExpiration().isBefore(maintenant)) {
                it.remove();
                System.out.println("OTP expiré supprimé pour : " + entry.getKey());
            }
        }
    }




}
