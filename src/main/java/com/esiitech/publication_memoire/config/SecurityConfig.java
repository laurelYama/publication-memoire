package com.esiitech.publication_memoire.config;

import com.esiitech.publication_memoire.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Classe de configuration de la sécurité Spring Security.
 * Gère les filtres de sécurité, l'authentification JWT, les droits d'accès selon les rôles, etc.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructeur avec injection du filtre JWT et du service d'utilisateur personnalisé.
     */
    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Chaîne de filtres de sécurité : configure les autorisations par URL, la gestion des sessions, etc.
     *
     * @param http configuration HTTP Security
     * @return la chaîne de filtres de sécurité
     * @throws Exception en cas d’erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Désactive la protection CSRF (utile en stateless API)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Pas de session : JWT uniquement
                .authorizeHttpRequests(auth -> auth

                        // Routes publiques
                        .requestMatchers("/api/auth/**").permitAll() // Connexion, inscription
                        .requestMatchers("/api/utilisateurs/activer-compte/**").permitAll() // Activation de compte
                        .requestMatchers("/api/memoires/recherche").permitAll()
                        .requestMatchers("/api/memoires/recherche-etudiant").permitAll()


                        .requestMatchers("/api/memoires/etudiant/soumettre").hasRole("ETUDIANT")
                        .requestMatchers("/api/memoires/lecteur/transmettre").hasRole("LECTEUR")
                        .requestMatchers("/api/memoires/{id}/telecharger").authenticated()
                        .requestMatchers("/api/memoires/{id}/telecharger/pdf").authenticated()
                        .requestMatchers("/api/memoires/{id}/preview/pdf").permitAll()

                        .requestMatchers("/api/memoires/{id}").authenticated()

                        .requestMatchers("/api/memoires/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/memoires/visibilite").hasRole("ADMIN")
                        .requestMatchers("/api/memoires/stats").authenticated()

                        // Utilisateur connecté requis
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/utilisateurs/changer-mot-de-passe").authenticated()
                        .requestMatchers("/api/historique/me").authenticated()

                        // Accès réservé à l’admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/historique/utilisateur/**").hasRole("ADMIN")

                        .requestMatchers("/api/memoires/lecteur/consulter").hasAnyRole("ADMIN", "LECTEUR")


                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider()) // Fournisseur d’authentification DAO
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Ajoute le filtre JWT avant le filtre standard
                .build();
    }

    /**
     * Fournisseur d’authentification basé sur le service utilisateur personnalisé et BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // Utilise notre service personnalisé
        provider.setPasswordEncoder(passwordEncoder());     // BCrypt pour hachage des mots de passe
        return provider;
    }

    /**
     * Bean de gestionnaire d'authentification.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Encodeur de mots de passe utilisant BCrypt (fort et sécurisé).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
