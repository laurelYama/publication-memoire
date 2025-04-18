package com.esiitech.publication_memoire.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // Clé secrète pour signer le token
    private final Key cleSecrete = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Durée de validité du token : 30 minutes
    private final long dureeExpiration = 1000 * 60 * 30;

    // Génère un token pour un utilisateur
    public String genererToken(UserDetails utilisateur) {
        Map<String, Object> claims = new HashMap<>();

        // Ajoute les rôles dans les claims du JWT
        claims.put("role", utilisateur.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return creerToken(claims, utilisateur.getUsername());
    }


    private String creerToken(Map<String, Object> claims, String nomUtilisateur) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(nomUtilisateur)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + dureeExpiration))
                .signWith(cleSecrete)
                .compact();
    }

    // Extrait le nom d'utilisateur depuis un token
    public String extraireNomUtilisateur(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    // Vérifie si le token est valide
    public boolean tokenValide(String token, UserDetails utilisateur) {
        final String nomUtilisateur = extraireNomUtilisateur(token);
        return nomUtilisateur.equals(utilisateur.getUsername()) && !tokenExpire(token);
    }

    // Vérifie si le token est expiré
    public boolean tokenExpire(String token) {
        return extraireDateExpiration(token).before(new Date());
    }

    // Extrait la date d’expiration
    public Date extraireDateExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    // Méthode générique pour extraire une info du token
    public <T> T extraireClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraireTousLesClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extraireTousLesClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(cleSecrete)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String extraireEmail(String token) {
        return extraireNomUtilisateur(token);
    }

}
