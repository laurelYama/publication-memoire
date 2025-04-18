Esiitech-Archive API
API REST pour la gestion des utilisateurs et des types de documents académiques (mémoires, thèses, etc.) développée avec Spring Boot.

📌 Fonctionnalités principales
Authentification et autorisation via Spring Security

Activation de compte par token

Changement de mot de passe

Gestion des utilisateurs (étudiants, admins)

Gestion des types de documents (mémoire, thèse, etc.)

Documentation interactive via Swagger UI

🔧 Technologies utilisées
Java 17

Spring Boot 3.4.4

Spring Security

Spring Data JPA

MySQL

Swagger / OpenAPI 3

Maven

🚀 Lancer l'application
1. Configuration de la base de données
Dans application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/publication_memoire
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
2. Démarrage
bash
./mvnw spring-boot:run

🔐 Sécurité
L’API utilise Spring Security. Les endpoints sont sécurisés avec les rôles suivants :

ROLE_ADMIN : Accès complet à tous les endpoints

Utilisateurs non authentifiés : Accès limité uniquement aux endpoints de création/activation de compte

📑 Documentation Swagger
L'interface Swagger est disponible à l'adresse :

http://localhost:8080/swagger-ui/index.html
La doc technique est générée automatiquement via OpenAPI 3.

🔁 Endpoints utiles

Méthode	Endpoint	Description	Rôle requis
POST	/api/utilisateurs/activer-compte/{token}	Activer un compte avec mot de passe	Public
PUT	/api/utilisateurs/changer-mot-de-passe	Changer son mot de passe	Authentifié
GET	/api/types	Lister tous les types de documents	ADMIN
POST	/api/types	Créer un type de document	ADMIN
PUT	/api/types/{id}	Modifier un type de document	ADMIN
DELETE	/api/types/{id}	Supprimer un type de document	ADMIN
⚙️ Configuration Swagger
Swagger est accessible même en environnement sécurisé. Voici les endpoints explicitement autorisés dans la config Spring Security :

java
.requestMatchers(
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/swagger-ui.html",
    "/v3/api-docs.yaml"
).permitAll()
🧑‍💻 Auteur
Nom : [Laurel YAMA et Feldy MOUNDZEGOU]

Projet universitaire – ESIITECH
