package StoryProject.controller;

import StoryProject.model.Personnage;
import StoryProject.model.Scene;
import StoryProject.model.StatistiquesHistoire;
import StoryProject.model.StatutScene;
import StoryProject.model.Story;
import StoryProject.repository.DBManager;
import StoryProject.repository.PersonnageRepository;
import StoryProject.repository.SceneRepository;
import StoryProject.repository.StoryRepository;
import StoryProject.service.SceneFilterService;
import StoryProject.service.PersonnageService;
import StoryProject.service.PdfExportService;
import StoryProject.service.SceneService;
import StoryProject.service.SceneStatsService;
import StoryProject.service.StoryService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StoryController implements Initializable {

    @FXML private Label  lblEnteteMode;
    @FXML private Button btnEnteteRetourAccueil;

    // --- Filtres de scènes (une seule barre) ---
    @FXML private ComboBox<StatutScene> cboSceneFiltrageScene;
    @FXML private ComboBox<Personnage>  cboFiltrePersonnageScene;
    @FXML private TextField             txtRechercheScene;
    @FXML private Button                btnAnnulerFiltrageScene;

    // --- Statistiques de l'histoire ---
    @FXML private Label lblStatNbPersonnages;
    @FXML private Label lblStatNbScenes;
    @FXML private Label lblStatScenesParStatut;

    @FXML private TextField txtSaisieTitre;
    @FXML private Label     lblErreurTitre;
    @FXML private TextField txtSaisieAuteur;
    @FXML private Label     lblErreurAuteur;
    @FXML private ComboBox<Story.GenreHistoire> cboGenreHistoire;
    @FXML private Label     lblErreurGenre;
    @FXML private TextArea  txtSaisieResume;
    @FXML private Label     lblCompteurCaracteres;

    @FXML private Button btnSauvegarderHistoire;
    @FXML private Button btnAnnulerHistoire;
    @FXML private Button btnExporterPdf;
    @FXML private Button btnSupprimerHistoire;

    @FXML private Label   lblTitreListePersonnages;
    @FXML private Button  btnAjouterPersonnage;
    @FXML private ListView<Personnage> listAffichagePersonnages;
    @FXML private Label   lblEtatVidePersonnages;
    @FXML private HBox    hboxActionsPersonnage;
    @FXML private Button  btnModifierPersonnage;
    @FXML private Button  btnSupprimerPersonnage;

    @FXML private Label   lblTitreListeScenes;
    @FXML private Button  btnAjouterScene;
    @FXML private ListView<Scene> listAffichageScenes;
    @FXML private Label   lblEtatVideScenes;
    @FXML private HBox    hboxActionsScene;
    @FXML private Button  btnModifierScene;
    @FXML private Button  btnSupprimerScene;

    private StoryService      storyService;
    private PersonnageService personnageService;
    private SceneService      sceneService;

    // Services métier (statistiques + filtrage), toute la logique y est faite
    private final SceneStatsService  sceneStatsService  = new SceneStatsService();
    private final SceneFilterService sceneFilterService = new SceneFilterService();
    private final PdfExportService   pdfExportService    = new PdfExportService();

    // Données chargées en mémoire pour l'histoire courante
    private List<Scene>      toutesLesScenes     = new ArrayList<>();
    private List<Personnage> personnagesHistoire = new ArrayList<>();

    private Story   storyEnCours;
    private boolean modeEdition = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        storyService = new StoryService(new StoryRepository(DBManager.getInstance()));
        personnageService = new PersonnageService(new PersonnageRepository(DBManager.getInstance()));
        sceneService = new SceneService(new SceneRepository(DBManager.getInstance()), new PersonnageRepository(DBManager.getInstance()));

        txtSaisieResume.textProperty().addListener((obs, oldVal, newVal) ->
                lblCompteurCaracteres.setText(newVal.length() + " caractères"));

        listAffichagePersonnages.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selectionne = newVal != null;
            hboxActionsPersonnage.setVisible(selectionne);
            hboxActionsPersonnage.setManaged(selectionne);
        });

        listAffichagePersonnages.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Personnage selection = listAffichagePersonnages.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    filterScenesByCharacter(selection);
                }
            }
        });

        listAffichageScenes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selectionne = newVal != null;
            hboxActionsScene.setVisible(selectionne);
            hboxActionsScene.setManaged(selectionne);
        });

        hboxActionsPersonnage.setVisible(false);
        hboxActionsPersonnage.setManaged(false);
        hboxActionsScene.setVisible(false);
        hboxActionsScene.setManaged(false);

        btnExporterPdf.setVisible(false);
        btnExporterPdf.setManaged(false);
        btnSupprimerHistoire.setVisible(false);
        btnSupprimerHistoire.setManaged(false);

        cboSceneFiltrageScene.getItems().addAll(StatutScene.values());
        cboGenreHistoire.getItems().addAll(Story.GenreHistoire.values());

        // Les filtres se ré-appliquent automatiquement dès qu'un critère change
        cboSceneFiltrageScene.valueProperty().addListener((o, a, b) -> applyFilters());
        cboFiltrePersonnageScene.valueProperty().addListener((o, a, b) -> applyFilters());
        txtRechercheScene.textProperty().addListener((o, a, b) -> applyFilters());
    }

    public void initWithStory(Story story) {
        this.storyEnCours = story;
        this.modeEdition = true;

        txtSaisieTitre.setText(story.getTitle());
        txtSaisieAuteur.setText(story.getAuthor());
        txtSaisieResume.setText(story.getSummary());
        cboGenreHistoire.setValue(story.getStoryGenre());

        lblEnteteMode.setText("Modifier l'histoire");
        btnExporterPdf.setVisible(true);
        btnExporterPdf.setManaged(true);
        btnSupprimerHistoire.setVisible(true);
        btnSupprimerHistoire.setManaged(true);

        loadCharacters();
        loadScenes();
    }

    public void initForCreation() {
        this.modeEdition = false;
        lblEnteteMode.setText("Nouvelle histoire");
        btnExporterPdf.setVisible(false);
        btnExporterPdf.setManaged(false);
        btnSupprimerHistoire.setVisible(false);
        btnSupprimerHistoire.setManaged(false);
    }

    @FXML
    private void onBtnSaveStory() {
        if (!validateForm()) {
            return;
        }

        String titre = txtSaisieTitre.getText().trim();
        String auteur = txtSaisieAuteur.getText().trim();
        String resume = txtSaisieResume.getText();
        Story.GenreHistoire genreHistoire = cboGenreHistoire.getValue();

        try {
            if (modeEdition) {
                storyEnCours.setTitle(titre);
                storyEnCours.setAuthor(auteur);
                storyEnCours.setSummary(resume);
                storyEnCours.setStoryGenre(genreHistoire);
                storyService.updateStory(storyEnCours);
            } else {
                storyEnCours = storyService.createStory(titre, genreHistoire, auteur, resume);
            }
            closeWindow();
        } catch (SQLException e) {
            showError("Impossible d'enregistrer l'histoire : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBtnCancelStory() {
        closeWindow();
    }

    @FXML
    private void onBtnDeleteStory() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer l'histoire \"" + storyEnCours.getTitle() + "\" ?");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                storyService.deleteStory(storyEnCours.getId());
                closeWindow();
            } catch (SQLException e) {
                showError("Impossible de supprimer l'histoire : " + e.getMessage());
            }
        }
    }

    // Export PDF : récupère les scènes publiées, choisit un fichier de destination,
    // puis délègue la génération à un Task exécuté hors du thread JavaFX.

    @FXML
    private void onBtnExportPdf() {
        List<Scene> scenesPubliees = sceneFilterService.filterByStatus(toutesLesScenes, StatutScene.PUBLIE);
        if (scenesPubliees.isEmpty()) {
            Alert info = new Alert(AlertType.INFORMATION);
            info.setTitle("Export PDF");
            info.setHeaderText(null);
            info.setContentText("Aucune scène publiée pour cette histoire, rien à exporter.");
            info.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'export PDF");
        fileChooser.setInitialFileName(storyEnCours.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        File destination = fileChooser.showSaveDialog(btnExporterPdf.getScene().getWindow());
        if (destination == null) {
            return;
        }

        // id de personnage -> "Prénom Nom", pour que le PDF affiche des noms plutôt que des identifiants
        Map<Long, String> nomsPersonnages = personnagesHistoire.stream()
                .collect(Collectors.toMap(Personnage::getId,
                        p -> p.getFirstName() + " " + p.getLastName()));

        Task<Void> tacheExport = new Task<>() {
            @Override
            protected Void call() throws IOException {
                pdfExportService.exportStory(storyEnCours, scenesPubliees, nomsPersonnages, destination);
                return null;
            }
        };

        tacheExport.setOnRunning(e -> {
            btnExporterPdf.setDisable(true);
            btnExporterPdf.setText("Export en cours...");
        });

        tacheExport.setOnSucceeded(e -> {
            btnExporterPdf.setDisable(false);
            btnExporterPdf.setText("Exporter en PDF");
            Alert succes = new Alert(AlertType.INFORMATION);
            succes.setTitle("Export PDF");
            succes.setHeaderText(null);
            succes.setContentText("Le PDF a bien été généré : " + destination.getAbsolutePath());
            succes.showAndWait();
        });

        tacheExport.setOnFailed(e -> {
            btnExporterPdf.setDisable(false);
            btnExporterPdf.setText("Exporter en PDF");
            showError("Impossible de générer le PDF : " + tacheExport.getException().getMessage());
        });

        Thread thread = new Thread(tacheExport, "export-pdf");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onBtnAddCharacter() {
        if (storyEnCours == null) {
            showError("Veuillez d'abord enregistrer l'histoire avant d'ajouter un personnage");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/personnage-form.fxml"));
            Parent root = loader.load();

            PersonnageController controller = loader.getController();
            controller.initForCreation(storyEnCours);

            Stage stage = new Stage();
            stage.setTitle("Nouveau personnage");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMinWidth(920);
            stage.setMinHeight(620);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCharacters();
            loadScenes();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire du personnage : " + e.getMessage());
        }
    }

    @FXML
    private void onBtnEditCharacter() {
        Personnage selection = listAffichagePersonnages.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/personnage-form.fxml"));
            Parent root = loader.load();

            PersonnageController controller = loader.getController();
            controller.initWithCharacter(selection, storyEnCours);

            Stage stage = new Stage();
            stage.setTitle("Modifier le personnage");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCharacters();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire du personnage : " + e.getMessage());
        }
    }

    @FXML
    private void onBtnDeleteCharacter() {
        Personnage selection = listAffichagePersonnages.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer le personnage \"" + selection.getLastName() + "\" ?");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                personnageService.deleteCharacter(selection.getId());
                loadCharacters();
                loadScenes();
            } catch (SQLException e) {
                showError("Impossible de supprimer le personnage : " + e.getMessage());
            }
        }
    }

    @FXML
    private void onBtnAddScene() {
        if (storyEnCours == null) {
            showError("Veuillez d'abord enregistrer l'histoire avant d'ajouter une scène");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/scene-form.fxml"));
            Parent root = loader.load();

            SceneController controller = loader.getController();
            controller.initForCreation(storyEnCours);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle scène");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadScenes();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire de la scène : " + e.getMessage());
        }
    }

    @FXML
    private void onBtnEditScene() {
        Scene selection = listAffichageScenes.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoryProject/view/scene-form.fxml"));
            Parent root = loader.load();

            SceneController controller = loader.getController();
            controller.initWithScene(selection, storyEnCours);

            Stage stage = new Stage();
            stage.setTitle("Modifier la scène");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadScenes();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire de la scène : " + e.getMessage());
        }
    }

    @FXML
    private void onBtnDeleteScene() {
        Scene selection = listAffichageScenes.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cette scène ?");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                sceneService.deleteScene(selection.getId());
                loadScenes();
            } catch (SQLException e) {
                showError("Impossible de supprimer la scène : " + e.getMessage());
            }
        }
    }

    @FXML
    private void onBtnHeaderBackHome() {
        closeWindow();
    }

    private boolean validateForm() {
        boolean valide = true;

        if (txtSaisieTitre.getText() == null || txtSaisieTitre.getText().trim().isEmpty()) {
            lblErreurTitre.setVisible(true);
            lblErreurTitre.setManaged(true);
            valide = false;
        } else {
            lblErreurTitre.setVisible(false);
            lblErreurTitre.setManaged(false);
        }

        if (txtSaisieAuteur.getText() == null || txtSaisieAuteur.getText().trim().isEmpty()) {
            lblErreurAuteur.setVisible(true);
            lblErreurAuteur.setManaged(true);
            valide = false;
        } else {
            lblErreurAuteur.setVisible(false);
            lblErreurAuteur.setManaged(false);
        }

        if (cboGenreHistoire.getValue() == null) {
            lblErreurGenre.setVisible(true);
            lblErreurGenre.setManaged(true);
            valide = false;
        } else {
            lblErreurGenre.setVisible(false);
            lblErreurGenre.setManaged(false);
        }

        return valide;
    }

    // Chargement des données de l'histoire courante

    private void loadCharacters() {
        try {
            List<Personnage> personnages = personnageService.getCharactersByStory(storyEnCours.getId());
            personnagesHistoire = personnages;
            listAffichagePersonnages.setItems(FXCollections.observableArrayList(personnages));

            // Alimente le ComboBox de filtre par personnage (en gardant la sélection si possible)
            Personnage selection = cboFiltrePersonnageScene.getValue();
            cboFiltrePersonnageScene.setItems(FXCollections.observableArrayList(personnages));
            if (selection != null) {
                personnages.stream()
                        .filter(p -> p.getId().equals(selection.getId()))
                        .findFirst()
                        .ifPresent(cboFiltrePersonnageScene::setValue);
            }

            boolean estVide = personnages.isEmpty();
            lblEtatVidePersonnages.setVisible(estVide);
            lblEtatVidePersonnages.setManaged(estVide);
            listAffichagePersonnages.setVisible(!estVide);
            listAffichagePersonnages.setManaged(!estVide);

            updateStats();
        } catch (SQLException e) {
            showError("Impossible de charger les personnages : " + e.getMessage());
        }
    }

    private void loadScenes() {
        try {
            List<Scene> scenes = sceneService.getScenesByStory(storyEnCours.getId());

            // Renseigne, pour chaque scène, les ids de ses personnages liés
            // (indispensable au filtrage par personnage en mémoire).
            for (Scene s : scenes) {
                List<Long> ids = sceneService.getCharactersByScene(s.getId())
                        .stream().map(Personnage::getId).collect(Collectors.toList());
                s.setCharacterIds(ids);
            }

            toutesLesScenes = scenes;   // liste maîtresse (scènes de CETTE histoire)
            updateStats();
            applyFilters();
        } catch (SQLException e) {
            showError("Impossible de charger les scènes : " + e.getMessage());
        }
    }

    // Recherche et filtrage des scènes (logique dans SceneFilterService)

    private void applyFilters() {
        if (toutesLesScenes == null) return;

        StatutScene statut  = cboSceneFiltrageScene.getValue();
        Personnage  perso   = cboFiltrePersonnageScene.getValue();
        Long        persoId = (perso == null) ? null : perso.getId();
        String      motCle  = txtRechercheScene.getText();

        List<Scene> resultat = sceneFilterService.applyFilters(toutesLesScenes, statut, persoId, motCle);
        showScenes(resultat);
    }

    private void showScenes(List<Scene> scenes) {
        listAffichageScenes.setItems(FXCollections.observableArrayList(scenes));
        boolean estVide = scenes.isEmpty();
        lblEtatVideScenes.setVisible(estVide);
        lblEtatVideScenes.setManaged(estVide);
        listAffichageScenes.setVisible(!estVide);
        listAffichageScenes.setManaged(!estVide);
    }

    /** Double-clic sur un personnage : positionne le filtre correspondant. */
    private void filterScenesByCharacter(Personnage personnage) {
        cboFiltrePersonnageScene.setValue(personnage); // déclenche applyFilters()
    }

    @FXML
    private void onBtnClearSceneFilters() {
        cboSceneFiltrageScene.getSelectionModel().clearSelection();
        cboFiltrePersonnageScene.getSelectionModel().clearSelection();
        txtRechercheScene.clear();
        applyFilters(); // tous les critères vides -> toutes les scènes
    }

    // Statistiques, mises à jour à chaque changement de données

    private void updateStats() {
        if (lblStatNbScenes == null) return; // sécurité si un label manque dans le FXML

        StatistiquesHistoire stats =
                sceneStatsService.buildStats(personnagesHistoire, toutesLesScenes);

        lblStatNbPersonnages.setText("Personnages : " + stats.characterCount());
        lblStatNbScenes.setText("Scènes : " + stats.sceneCount());

        Map<StatutScene, Long> ps = stats.scenesByStatus();
        lblStatScenesParStatut.setText(
                "Brouillon : " + ps.get(StatutScene.BROUILLON)
              + "    En cours : " + ps.get(StatutScene.EN_COURS)
              + "    Terminé : " + ps.get(StatutScene.TERMINE));
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAnnulerHistoire.getScene().getWindow();
        stage.close();
    }
}
