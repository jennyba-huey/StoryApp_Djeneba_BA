package StoryProject.controller;

import StoryProject.model.Personnage;
import StoryProject.model.StatistiquesGlobales;
import StoryProject.model.StatistiquesHistoire;
import StoryProject.model.StatutScene;
import StoryProject.model.Story;
import StoryProject.repository.DBManager;
import StoryProject.repository.PersonnageRepository;
import StoryProject.repository.SceneRepository;
import StoryProject.repository.StoryRepository;
import StoryProject.service.GlobalStatsService;
import StoryProject.service.PersonnageService;
import StoryProject.service.SceneService;
import StoryProject.service.SceneStatsService;
import StoryProject.service.StoryService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Label     lblNavTitreApp;
    @FXML private Button    btnNavNouvelleHistoire;
    @FXML private Button    btnNavStatistiques;
    @FXML private TextField txtNavRechercheHistoire;
    @FXML private Label     lblNavNombreHistoires;

    // --- Statistiques (globales ou d'une histoire sélectionnée) ---
    @FXML private Label lblStatCategorie;
    @FXML private Label lblRetourGlobal;
    @FXML private Label lblStatHistoires;
    @FXML private Label lblStatPersonnages;
    @FXML private Label lblStatScenes;
    @FXML private Label lblStatTermine;

    @FXML private VBox vboxListeHistoires;
    @FXML private VBox vboxListePersonnages;
    @FXML private VBox vboxListeScenes;

    @FXML private GridPane  gridAffichageHistoires;
    @FXML private VBox      vboxEtatVide;
    @FXML private Button    btnEtatVideCreerHistoire;

    @FXML private Label     lblPiedVersion;

    private StoryService       storyService;
    private GlobalStatsService globalStatsService;
    private PersonnageService  personnageService;
    private SceneService       sceneService;
    private SceneStatsService  sceneStatsService;

    /** Histoire actuellement sélectionnée dans la grille (null = vue globale). */
    private Story storySelectionnee;
    /** Carte visuelle actuellement mise en surbrillance, pour pouvoir la désélectionner. */
    private VBox carteSelectionnee;

    private static final int COLONNES_GRILLE = 3;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        storyService       = new StoryService(new StoryRepository(DBManager.getInstance()));
        sceneService       = new SceneService(new SceneRepository(DBManager.getInstance()),
                new PersonnageRepository(DBManager.getInstance()));
        personnageService  = new PersonnageService(new PersonnageRepository(DBManager.getInstance()));
        sceneStatsService  = new SceneStatsService();

        globalStatsService = new GlobalStatsService(storyService, sceneService, personnageService);

        loadStories();
        txtNavRechercheHistoire.setOnKeyReleased(e -> onTxtNavSearchStory());
    }

    @FXML
    private void onBtnNavNewStory() {
        openCreationForm();
    }

    @FXML
    private void onBtnNavStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Statistiques");
            stage.setScene(new Scene(root));
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.show();
        } catch (IOException e) {
            showError("Impossible d'ouvrir les statistiques : " + e.getMessage());
        }
    }

    @FXML
    private void onBtnEmptyStateCreateStory() {
        openCreationForm();
    }

    @FXML
    private void onTxtNavSearchStory() {
        String motCle = txtNavRechercheHistoire.getText();
        try {
            List<Story> resultats = (motCle == null || motCle.trim().isEmpty())
                    ? storyService.getAllStories()
                    : storyService.findByTitle(motCle.trim());
            showStories(resultats);
        } catch (SQLException e) {
            showError("Impossible de rechercher les histoires : " + e.getMessage());
        }
    }

    private void loadStories() {
        try {
            // Un rechargement complet régénère les cartes : l'ancienne référence de
            // surbrillance n'a plus de sens, on repart donc sur la vue globale.
            storySelectionnee = null;
            carteSelectionnee = null;
            showStories(storyService.getAllStories());
            updateGlobalStats();
        } catch (SQLException e) {
            showError("Impossible de charger les histoires : " + e.getMessage());
        }
    }

    // Statistiques : globales ou d'une histoire sélectionnée

    /** Affiche les statistiques de tout l'atelier (comportement par défaut). */
    private void updateGlobalStats() {
        if (lblStatHistoires == null) return;
        try {
            StatistiquesGlobales s = globalStatsService.compute();
            lblStatCategorie.setText("STATISTIQUES");
            lblRetourGlobal.setVisible(false);
            lblRetourGlobal.setManaged(false);
            lblStatHistoires.setText(String.valueOf(s.storyCount()));
            lblStatPersonnages.setText(String.valueOf(s.characterCount()));
            lblStatScenes.setText(String.valueOf(s.sceneCount()));
            lblStatTermine.setText(s.completionPercentage() + " %");
            closeAllDropdowns();
        } catch (SQLException e) {
            showError("Impossible de calculer les statistiques : " + e.getMessage());
        }
    }

    /** Affiche les statistiques d'une histoire précise dans la sidebar. */
    private void showStoryStats(Story story) {
        try {
            List<Personnage> personnages = personnageService.getCharactersByStory(story.getId());
            List<StoryProject.model.Scene> scenes = sceneService.getScenesByStory(story.getId());
            StatistiquesHistoire stats = sceneStatsService.buildStats(personnages, scenes);

            long nbTermine = stats.scenesByStatus().getOrDefault(StatutScene.TERMINE, 0L);
            int pourcentage = stats.sceneCount() == 0
                    ? 0
                    : (int) Math.round(100.0 * nbTermine / stats.sceneCount());

            lblStatCategorie.setText("HISTOIRE : " + story.getTitle().toUpperCase());
            lblRetourGlobal.setVisible(true);
            lblRetourGlobal.setManaged(true);
            lblStatHistoires.setText("1");
            lblStatPersonnages.setText(String.valueOf(stats.characterCount()));
            lblStatScenes.setText(String.valueOf(stats.sceneCount()));
            lblStatTermine.setText(pourcentage + " %");
            closeAllDropdowns();
        } catch (SQLException e) {
            showError("Impossible de calculer les statistiques de l'histoire : " + e.getMessage());
        }
    }

    /** Appelé quand l'utilisateur clique sur une carte d'histoire dans la grille. */
    private void selectStory(Story story, VBox carte) {
        if (carteSelectionnee != null) {
            carteSelectionnee.getStyleClass().remove("story-card-selected");
        }
        carte.getStyleClass().add("story-card-selected");
        carteSelectionnee = carte;
        storySelectionnee = story;
        showStoryStats(story);
    }

    /** Lien "← Vue globale" dans la sidebar : annule la sélection. */
    @FXML
    private void onBackToGlobalStats(MouseEvent event) {
        if (carteSelectionnee != null) {
            carteSelectionnee.getStyleClass().remove("story-card-selected");
            carteSelectionnee = null;
        }
        storySelectionnee = null;
        updateGlobalStats();
    }

    // Listes déroulantes cliquables sur les stats

    @FXML
    private void onStatStoriesClick(MouseEvent event) {
        toggleDropdown(vboxListeHistoires, () -> {
            try {
                if (storySelectionnee != null) {
                    // Une histoire est sélectionnée : n'afficher qu'elle
                    Label item = new Label("• " + storySelectionnee.getTitle());
                    item.getStyleClass().add("kpi-dropdown-item");
                    vboxListeHistoires.getChildren().add(item);
                } else {
                    // Vue globale : toutes les histoires
                    for (Story story : storyService.getAllStories()) {
                        Label item = new Label("• " + story.getTitle());
                        item.getStyleClass().add("kpi-dropdown-item");
                        item.setOnMouseClicked(e -> showStoryStats(story));
                        vboxListeHistoires.getChildren().add(item);
                    }
                }
            } catch (SQLException e) {
                showError("Impossible de charger les histoires : " + e.getMessage());
            }
        });
    }

    @FXML
    private void onStatCharactersClick(MouseEvent event) {
        toggleDropdown(vboxListePersonnages, () -> {
            try {
                if (storySelectionnee != null) {
                    // Une histoire est sélectionnée : n'afficher que SES personnages
                    for (Personnage p : personnageService.getCharactersByStory(storySelectionnee.getId())) {
                        addCharacterItem(p);
                    }
                } else {
                    // Vue globale : tous les personnages de toutes les histoires
                    for (Story story : storyService.getAllStories()) {
                        for (Personnage p : personnageService.getCharactersByStory(story.getId())) {
                            addCharacterItem(p);
                        }
                    }
                }
            } catch (SQLException e) {
                showError("Impossible de charger les personnages : " + e.getMessage());
            }
        });
    }

    private void addCharacterItem(Personnage p) {
        Label item = new Label("• " + p.getFirstName() + " " + p.getLastName());
        item.getStyleClass().add("kpi-dropdown-item");
        vboxListePersonnages.getChildren().add(item);
    }

    @FXML
    private void onStatScenesClick(MouseEvent event) {
        toggleDropdown(vboxListeScenes, () -> {
            try {
                if (storySelectionnee != null) {
                    // Une histoire est sélectionnée : n'afficher que SES scènes
                    for (StoryProject.model.Scene scene : sceneService.getScenesByStory(storySelectionnee.getId())) {
                        addSceneItem(scene);
                    }
                } else {
                    // Vue globale : toutes les scènes de toutes les histoires
                    for (Story story : storyService.getAllStories()) {
                        for (StoryProject.model.Scene scene : sceneService.getScenesByStory(story.getId())) {
                            addSceneItem(scene);
                        }
                    }
                }
            } catch (SQLException e) {
                showError("Impossible de charger les scènes : " + e.getMessage());
            }
        });
    }

    private void addSceneItem(StoryProject.model.Scene scene) {
        Label item = new Label("• " + scene.getTitle());
        item.getStyleClass().add("kpi-dropdown-item");
        vboxListeScenes.getChildren().add(item);
    }

    private void toggleDropdown(VBox dropdown, Runnable populate) {
        boolean estVisible = dropdown.isVisible();
        dropdown.getChildren().clear();
        if (estVisible) {
            dropdown.setVisible(false);
            dropdown.setManaged(false);
        } else {
            populate.run();
            dropdown.setVisible(true);
            dropdown.setManaged(true);
        }
    }

    /** Referme et vide les trois listes déroulantes (utilisé lors d'un changement de contexte). */
    private void closeAllDropdowns() {
        for (VBox dropdown : List.of(vboxListeHistoires, vboxListePersonnages, vboxListeScenes)) {
            dropdown.setVisible(false);
            dropdown.setManaged(false);
            dropdown.getChildren().clear();
        }
    }

    // Grille des histoires

    private void showStories(List<Story> histoires) {
        gridAffichageHistoires.getChildren().clear();

        boolean estVide = histoires.isEmpty();
        vboxEtatVide.setVisible(estVide);
        vboxEtatVide.setManaged(estVide);
        gridAffichageHistoires.setVisible(!estVide);
        gridAffichageHistoires.setManaged(!estVide);

        int colonne = 0;
        int ligne   = 0;
        for (Story story : histoires) {
            gridAffichageHistoires.add(createStoryCard(story), colonne, ligne);
            colonne++;
            if (colonne >= COLONNES_GRILLE) {
                colonne = 0;
                ligne++;
            }
        }

        updateCounter(histoires.size());
    }

    private VBox createStoryCard(Story story) {
        VBox carte = new VBox();
        carte.getStyleClass().add("story-card");

        Label titreLabel = new Label(story.getTitle());
        titreLabel.getStyleClass().add("card-titre");

        Label auteurLabel = new Label(story.getAuthor());
        auteurLabel.getStyleClass().add("card-auteur");

        String genreTexte = story.getStoryGenre() != null ? story.getStoryGenre().name() : "";
        Label genreLabel = new Label(genreTexte);
        genreLabel.getStyleClass().add("card-genre");

        String resume        = story.getSummary() == null ? "" : story.getSummary();
        String resumeTronque = resume.length() > 100 ? resume.substring(0, 100) + "..." : resume;
        Label resumeLabel    = new Label(resumeTronque);
        resumeLabel.getStyleClass().add("card-resume");
        resumeLabel.setWrapText(true);

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().add("card-btn-modifier");
        btnModifier.setOnAction(e -> {
            e.consume();
            openStoryDetail(story);
        });

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger-sm");
        btnSupprimer.setOnAction(e -> {
            e.consume();
            deleteStory(story);
        });

        HBox boutons = new HBox(8, btnModifier, btnSupprimer);
        boutons.getStyleClass().add("story-card-actions");

        carte.getChildren().addAll(titreLabel, auteurLabel, genreLabel, resumeLabel, boutons);

        // Cliquer sur la carte (hors boutons) sélectionne l'histoire et met à jour
        // les statistiques de la sidebar. Pour MODIFIER l'histoire, utiliser le bouton dédié.
        carte.setOnMouseClicked(e -> selectStory(story, carte));

        return carte;
    }

    private void openStoryDetail(Story story) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/story-form.fxml"));
            Parent root = loader.load();

            StoryController controller = loader.getController();
            controller.initWithStory(story);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'histoire");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadStories();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire de l'histoire : " + e.getMessage());
        }
    }

    private void openCreationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/story-form.fxml"));
            Parent root = loader.load();

            StoryController controller = loader.getController();
            controller.initForCreation();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle histoire");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadStories();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire de l'histoire : " + e.getMessage());
        }
    }

    private void deleteStory(Story story) {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer l'histoire \"" + story.getTitle() + "\" ?");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                storyService.deleteStory(story.getId());
                loadStories();
            } catch (SQLException e) {
                showError("Impossible de supprimer l'histoire : " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateCounter(int nombre) {
        lblNavNombreHistoires.setText(nombre + (nombre <= 1 ? " histoire" : " histoires"));
    }
}