-- Script de création de la base de données StoryProject
-- A exécuter en entier dans MySQL Workbench

CREATE DATABASE IF NOT EXISTS storyforge;
USE storyforge;

DROP TABLE IF EXISTS scene_personnage;
DROP TABLE IF EXISTS scene;
DROP TABLE IF EXISTS personnage;
DROP TABLE IF EXISTS story;

CREATE TABLE story (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre          VARCHAR(255) NOT NULL,
    auteur         VARCHAR(255) NOT NULL,
    resume         TEXT,
    genre_histoire VARCHAR(50)
);

CREATE TABLE personnage (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    story_id                 BIGINT NOT NULL,
    prenom                   VARCHAR(255),
    nom                      VARCHAR(255) NOT NULL,
    age                      INT,
    role                     ENUM('AntagonistePrincipal','AntagonisteSecondaire','ProtagonistePrincipal','ProtagonisteSecondaire','Figurant') NOT NULL,
    description_physique     TEXT,
    genre                    ENUM('Homme','Femme','NonBinaire') NOT NULL,
    description_personnalite TEXT,
    FOREIGN KEY (story_id) REFERENCES story(id) ON DELETE CASCADE
);

CREATE TABLE scene (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    story_id BIGINT NOT NULL,
    titre    VARCHAR(255) NOT NULL,
    ordre    INT NOT NULL,
    lieu     VARCHAR(255),
    moment   VARCHAR(50),
    resume   TEXT,
    statut   ENUM('BROUILLON','EN_COURS','TERMINE','PUBLIE') NOT NULL DEFAULT 'BROUILLON',
    tags     VARCHAR(500),
    FOREIGN KEY (story_id) REFERENCES story(id) ON DELETE CASCADE
);

CREATE TABLE scene_personnage (
    scene_id      BIGINT NOT NULL,
    personnage_id BIGINT NOT NULL,
    PRIMARY KEY (scene_id, personnage_id),
    FOREIGN KEY (scene_id)      REFERENCES scene(id)      ON DELETE CASCADE,
    FOREIGN KEY (personnage_id) REFERENCES personnage(id) ON DELETE CASCADE
);
