package com.esiitech.publication_memoire.service;

import com.esiitech.publication_memoire.dto.EtudiantDto;
import com.esiitech.publication_memoire.dto.TrombinoscopeAuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TrombinoscopeService {

    private final RestTemplate restTemplate;

    @Value("${trombinoscope.api.username}")
    private String username;

    @Value("${trombinoscope.api.password}")
    private String password;

    @Value("${trombinoscope.api.baseUrl}")
    private String baseUrl;

    public TrombinoscopeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String recupererToken() {
        String authUrl = baseUrl + "/api/auth/login";

        Map<String, String> credentials = Map.of(
                "email", username,
                "password", password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(credentials, headers);

        try {
            ResponseEntity<TrombinoscopeAuthResponse> response = restTemplate.postForEntity(authUrl, request, TrombinoscopeAuthResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody().getToken();
            } else {
                throw new RuntimeException("Erreur lors de l'authentification : " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération du token : " + e.getMessage());
        }
    }

    public EtudiantDto chercherEtudiant(String identifiant) {
        String etudiantUrl = baseUrl + "/api/etudiants/actifs";

        String token = recupererToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<EtudiantDto[]> response = restTemplate.exchange(etudiantUrl, HttpMethod.GET, request, EtudiantDto[].class);
            if (response.getStatusCode().is2xxSuccessful()) {
                EtudiantDto[] etudiants = response.getBody();
                if (etudiants != null) {
                    return findEtudiantByIdentifiant(etudiants, identifiant);
                } else {
                    throw new RuntimeException("Aucun étudiant actif trouvé.");
                }
            } else {
                throw new RuntimeException("Erreur HTTP lors de la requête : " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de l'étudiant : " + e.getMessage());
        }
    }

    private EtudiantDto findEtudiantByIdentifiant(EtudiantDto[] etudiants, String identifiant) {
        for (EtudiantDto etudiant : etudiants) {
            if (identifiant.equalsIgnoreCase(etudiant.getEmail()) || identifiant.equalsIgnoreCase(etudiant.getTelephone())) {
                return etudiant;
            }
        }
        throw new RuntimeException("Étudiant non trouvé.");
    }
}
