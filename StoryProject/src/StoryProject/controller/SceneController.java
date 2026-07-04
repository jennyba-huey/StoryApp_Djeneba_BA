package StoryProject.controller;

import StoryProject.model.Personnage;
import StoryProject.model.Scene;
import StoryProject.model.StatutScene;
import StoryProject.model.Story;
import StoryProject.repository.DBManager;
import StoryProject.repository.PersonnageRepository;
import StoryProject.repository.SceneRepository;
import StoryProject.service.PersonnageService;
import StoryProject.service.SceneService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SceneController implements Initializable {

    @FXML private Label  lblEnteteMode;
    @FXML private Label  lblEnteteNomHistoire;
    @FXML private Button btnEnteteRetourHistoire;

    @FXML private TextField txtSaisieTitreScene;
    @FXML private Label     lblErreurTitreScene;
    @FXML private TextField txtSaisieOrdreScene;
    @FXML private Label     lblErreurOrdreScene;
    @FXML private TextField txtSaisieLieuScene;
    @FXML private TextField txtSaisieMomentScene;
    @FXML private TextArea  txtSaisieResumeScene;
    @FXML private Label     lblCompteurCaracteres;

    @FXML private Label                 lblTitreStatutScene;
    @FXML private ComboBox<StatutScene> cboSelectionStatutScene;

    @FXML private Label     lblTitreTagsScene;
    @FXML private TextField txtSaisieAjoutTag;
    @FXML private Button    btnAjouterTag;
    @FXML private FlowPane  hboxAffichageTags;

    @FXML private Label                lblTitrePersonnagesScene;
    @FXML private ListView<Personnage> listSelectionPersonnagesScene;
    @FXML private Button               btnLierPersonnageScene;
    @FXML private ListView<Personnage> listAffichagePersonnagesLies;
    @FXML private Button               btnDelierPersonnageScene;

    @FXML private Button btnSauvegarderScene;
    @FXML private Button btnAnnulerScene;
    @FXML private Button btnSupprimerScene;

    private SceneService      sceneService;
    private PersonnageService personnageService;

    private Scene        sceneEnCours;
    private Story        storyParente;
    private boolean      modeEdition = false;
    private List<String> tagsActuels = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sceneService      = new SceneService(new SceneRepository(DBManager.getInstance()), new PersonnageRepository(DBManager.getInstance()));
        personnageService = new PersonnageService(new PersonnageRepository(DBManager.getInstance()));

        cboSelectionStatutScene.setItems(FXCollections.observableArrayList(StatutScene.values()));

        txtSaisieResumeScene.textProperty().addListener((obs, oldVal, newVal) ->
                lblCompteurCaracteres.setText(newVal.length() + " caractères"));

        txtSaisieAjoutTag.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) onBtnAddTag();
        });

        btnSupprimerScene.setVisible(false);
        btnSupprimerScene.setManaged(false);
    }

    public void initWithScene(Scene scene, Story story) {
        this.modeEdition  = true;
        this.sceneEnCours = scene;
        this.storyParente = story;

        txtSaisieTitreScene.setText(scene.getTitle());
        txtSaisieOrdreScene.setText(String.valueOf(scene.getOrder()));
        txtSaisieLieuScene.setText(scene.getLocation());
        txtSaisieMomentScene.setText(scene.getMoment());
        txtSaisieResumeScene.setText(scene.getSummary());
        cboSelectionStatutScene.setValue(scene.getStatus());

        hboxAffichageTags.getChildren().clear();
        tagsActuels = new ArrayList<>(scene.getTags());
        for (String tag : tagsActuels) addTagChip(tag);

        lblEnteteMode.setText("Modifier la scène");
        lblEnteteNomHistoire.setText(story.getTitle());

        btnSupprimerScene.setVisible(true);
        btnSupprimerScene.setManaged(true);

        loadAvailableCharacters();
        loadLinkedCharacters();
    }

    public void initForCreation(Story story) {
        this.modeEdition  = false;
        this.storyParente = story;

        lblEnteteMode.setText("Nouvelle scène");
        lblEnteteNomHistoire.setText(story.getTitle());
        cboSelectionStatutScene.setValue(StatutScene.BROUILLON);

        loadAvailableCharacters();
    }

    @FXML
    private void onBtnSaveScene() {
        if (!validateForm()) return;

        String      titre  = txtSaisieTitreScene.getText().trim();
        int         ordre  = Integer.parseInt(txtSaisieOrdreScene.getText().trim());
        String      lieu   = txtSaisieLieuScene.getText();
        String      moment = txtSaisieMomentScene.getText();
        String      resume = txtSaisieResumeScene.getText();
        StatutScene statut = cboSelectionStatutScene.getValue();

        try {
            if (sceneEnCours != null) {
                sceneEnCours.setTitle(titre);
                sceneEnCours.setOrder(ordre);
                sceneEnCours.setLocation(lieu);
                sceneEnCours.setMoment(moment);
                sceneEnCours.setSummary(resume);
                sceneEnCours.setStatus(statut);
                sceneEnCours.setTags(new ArrayList<>(tagsActuels));
                sceneService.updateScene(sceneEnCours);
            } else {
                Scene scene = sceneService.createScene(titre, storyParente.getId(), ordre);
                scene.setLocation(lieu);
                scene.setMoment(moment);
                scene.setSummary(resume);
                scene.setStatus(statut);
                scene.setTags(new ArrayList<>(tagsActuels));
                sceneService.updateScene(scene);
                sceneEnCours = scene;
            }
            closeWindow();
        } catch (SQLException e) {
            showError("Impossible d'enregistrer la scène : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBtnCancelScene() {
        closeWindow();
    }

    @FXML
    private void onBtnDeleteScene() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cette scène ?");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                sceneService.deleteScene(sceneEnCours.getId());
                closeWindow();
            } catch (SQLException e) {
                showError("Impossible de supprimer la scène : " + e.getMessage());
            }
        }
    }

    @FXML
    private void onBtnAddTag() {
        String tag = txtSaisieAjoutTag.getText() == null ? "" : txtSaisieAjoutTag.getText().trim();
        if (!tag.isEmpty() && !tagsActuels.contains(tag)) {
            tagsActuels.add(tag);
            addTagChip(tag);
            txtSaisieAjoutTag.clear();
        }
    }

    @FXML
    private void onBtnLinkCharacterToScene() {
        Personnage selection = listSelectionPersonnagesScene.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        if (sceneEnCours == null) {
            if (!validateForm()) return;
            try {
                String titre = txtSaisieTitreScene.getText().trim();
                int    ordre = Integer.parseInt(txtSaisieOrdreScene.getText().trim());
                sceneEnCours = sceneService.createScene(titre, storyParente.getId(), ordre);
            } catch (SQLException e) {
                showError("Impossible d'enregistrer la scène : " + e.getMessage());
                return;
            }
        }

        try {
            sceneService.linkCharacter(sceneEnCours.getId(), selection.getId());
            loadLinkedCharacters();
            loadAvailableCharacters();
        } catch (SQLException e) {
            showError("Impossible de lier le personnage : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBtnUnlinkCharacterFromScene() {
        Personnage selection = listAffichagePersonnagesLies.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        try {
            sceneService.unlinkCharacter(sceneEnCours.getId(), selection.getId());
            loadLinkedCharacters();
            loadAvailableCharacters();
        } catch (SQLException e) {
            showError("Impossible de délier le personnage : " + e.getMessage());
        }
    }

    @FXML
    private void onBtnHeaderBackToStory() {
        closeWindow();
    }

    private boolean validateForm() {
        boolean valide = true;

        if (txtSaisieTitreScene.getText() == null || txtSaisieTitreScene.getText().trim().isEmpty()) {
            lblErreurTitreScene.setVisible(true);
            lblErreurTitreScene.setManaged(true);
            valide = false;
        } else {
            lblErreurTitreScene.setVisible(false);
            lblErreurTitreScene.setManaged(false);
        }

        try {
            int ordre = Integer.parseInt(txtSaisieOrdreScene.getText().trim());
            if (ordre < 0) throw new NumberFormatException();
            lblErreurOrdreScene.setVisible(false);
            lblErreurOrdreScene.setManaged(false);
        } catch (NumberFormatException e) {
            lblErreurOrdreScene.setVisible(true);
            lblErreurOrdreScene.setManaged(true);
            valide = false;
        }

        return valide;
    }

    private void addTagChip(String tag) {
        Label  label        = new Label(tag);
        Button btnSupprimer = new Button("x");

        HBox chip = new HBox(label, btnSupprimer);
        chip.getStyleClass().add("tag-chip");

        btnSupprimer.setOnAction(e -> {
            tagsActuels.remove(tag);
            hboxAffichageTags.getChildren().remove(chip);
        });

        hboxAffichageTags.getChildren().add(chip);
    }

    private void loadAvailableCharacters() {
        try {
            List<Personnage> tous = personnageService.getCharactersByStory(storyParente.getId());
            List<Personnage> lies = listAffichagePersonnagesLies.getItems();
            tous.removeIf(p -> lies.stream().anyMatch(l -> l.getId().equals(p.getId())));
            listSelectionPersonnagesScene.setItems(FXCollections.observableArrayList(tous));
        } catch (SQLException e) {
            showError("Impossible de charger les personnages disponibles : " + e.getMessage());
        }
    }

    private void loadLinkedCharacters() {
        try {
            List<Personnage> lies = sceneService.getCharactersByScene(sceneEnCours.getId());
            listAffichagePersonnagesLies.setItems(FXCollections.observableArrayList(lies));
        } catch (SQLException e) {
            showError("Impossible de charger les personnages liés : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAnnulerScene.getScene().getWindow();
        stage.close();
    }
}