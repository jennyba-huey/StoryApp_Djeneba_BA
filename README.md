# StoryProject

## Autrice

- Djeneba Ba 👩🏾‍💻

## Description

StoryProject est une application JavaFX de gestion d'histoires, de personnages et de scènes.
Elle permet de créer, modifier et rechercher des histoires, de gérer les personnages et les scènes associées,
de consulter des statistiques globales et par histoire, et d'exporter les scènes publiées au format PDF.

## Technologies

- Java 21
- JavaFX 21
- Maven
- MySQL
- Apache PDFBox
- JUnit 5

## Structure du projet

- `StoryProject/pom.xml` : configuration Maven et dépendances.
- `StoryProject/src/StoryProject/app` : point d'entrée de l'application JavaFX.
- `StoryProject/src/StoryProject/controller` : contrôleurs JavaFX qui gèrent l'interface utilisateur.
- `StoryProject/src/StoryProject/model` : classes de domaine pour Story, Scene, Personnage, statistiques, et statuts.
- `StoryProject/src/StoryProject/repository` : accès à la base de données MySQL.
- `StoryProject/src/StoryProject/service` : logique métier et services transverses.
- `StoryProject/src/StoryProject/view` : fichiers FXML de l'interface.
- `StoryProject/src/StoryProject/resources/css` : styles CSS de l'application.
- `storyforge_schema.sql` : script de création de la base de données MySQL.

## Fonctionnalités principales

- Créer, modifier et supprimer des histoires.
- Ajouter, modifier et supprimer des personnages.
- Ajouter, modifier et supprimer des scènes.
- Rechercher des histoires par titre.
- Filtrer les scènes par statut et par personnage.
- Consulter des statistiques globales et spécifiques à une histoire.
- Exporter les scènes publiées d'une histoire en PDF.

## Installation

### Prérequis

- Java 21 installé.
- Maven installé.
- MySQL installé et démarré.

### Configuration de la base de données

1. Créez la base de données avec le script SQL `storyforge_schema.sql`.
2. Le script crée la base `storyforge` et les tables suivantes : `story`, `personnage`, `scene`, `scene_personnage`.

### Exemple d'exécution du script

Ouvrez MySQL Workbench ou un client MySQL, puis exécutez :

```sql
SOURCE path/to/storyforge_schema.sql;
```

### Paramètres de connexion

La configuration de connexion à MySQL se trouve dans :

- `StoryProject/src/StoryProject/repository/DBManager.java`

**Valeurs par défaut :**

- URL : `jdbc:mysql://localhost:3306/storyforge`
- Utilisateur : `root`
- Mot de passe : `root`

> Ajustez ces valeurs si votre configuration MySQL diffère.

## Compilation et exécution

Depuis le dossier `StoryProject` :

```bash
cd "c:\Users\djene\Documents\BDML1_Ingé1\Java Avancé\StoryProject_Thread_Djeneba_BA\StoryProject"
mvn clean package
mvn javafx:run
```

> Si vous souhaitez exécuter le jar, utilisez le plugin JavaFX ou un launcher adapté aux modules JavaFX.

## Tests

Exécutez les tests avec :

```bash
mvn test
```

## Points spécifiques

- Point d'entrée principal : `StoryProject.app.StoryProjectApp`.
- Launcher Java standard : `StoryProject.app.Launcher`.
- Vues FXML : `StoryProject/src/StoryProject/view`.
- Export PDF : `StoryProject/src/StoryProject/service/PdfExportService.java`.

## Notes

- L'application ferme la connexion MySQL lors de la fermeture de la fenêtre principale.
- Le code suit un modèle MVC simplifié : modèles, contrôleurs, services et dépôts.
- La base de données utilise des clés étrangères pour assurer l'intégrité entre histoires, scènes et personnages.
