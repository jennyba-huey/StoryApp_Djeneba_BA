package StoryProject.controller;

import StoryProject.model.Personnage;
import StoryProject.model.Story;
import StoryProject.repository.DBManager;
import StoryProject.repository.PersonnageRepository;
import StoryProject.service.PersonnageService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class PersonnageController implements Initializable {

    @FXML private Label     lblEnteteMode;
    @FXML private Label     lblEnteteNomHistoire;

    @FXML private TextField txtSaisiePrenom;
    @FXML private Label     lblErreurPrenom;

    @FXML private TextField txtSaisieNomPersonnage;
    @FXML private Label     lblErreurNomPersonnage;

    @FXML private TextField txtSaisieAge;
    @FXML private Label     lblErreurAge;

    @FXML private ComboBox<Personnage.Role>  cboSelectionRole;
    @FXML private Label                      lblErreurRole;

    @FXML private ComboBox<Personnage.Genre> cboGenre;
    @FXML private Label                      lblErreurRole1;

    @FXML private TextArea txtSaisieDescriptionPersonnagePhysique;
    @FXML private Label    lblCompteurCaracteres1;

    @FXML private TextArea txtSaisieDescriptionPersonnage;
    @FXML private Label    lblCompteurCaracteres;

    @FXML private Button btnSauvegarderPersonnage;
    @FXML private Button btnAnnulerPersonnage;
    @FXML private Button btnSupprimerPersonnage;

    private PersonnageService personnageService;
    private Personnage         personnageEnCours;
    private Story              storyParente;
    private boolean            modeEdition = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        personnageService = new PersonnageService(new PersonnageRepository(DBManager.getInstance()));

        cboSelectionRole.setItems(FXCollections.observableArrayList(Personnage.Role.values()));
        cboGenre.setItems(FXCollections.observableArrayList(Personnage.Genre.values()));

        txtSaisieDescriptionPersonnage.textProperty().addListener((obs, oldVal, newVal) ->
                lblCompteurCaracteres.setText(newVal.length() + " caractères"));

        txtSaisieDescriptionPersonnagePhysique.textProperty().addListener((obs, oldVal, newVal) ->
                lblCompteurCaracteres1.setText(newVal.length() + " caractères"));

        btnSupprimerPersonnage.setVisible(false);
        btnSupprimerPersonnage.setManaged(false);
    }

    public void initWithCharacter(Personnage personnage, Story story) {
        this.modeEdition       = true;
        this.personnageEnCours = personnage;
        this.storyParente      = story;

        txtSaisiePrenom.setText(personnage.getFirstName());
        txtSaisieNomPersonnage.setText(personnage.getLastName());
        txtSaisieAge.setText(String.valueOf(personnage.getAge()));
        cboSelectionRole.setValue(personnage.getRole());
        cboGenre.setValue(personnage.getGender());
        txtSaisieDescriptionPersonnagePhysique.setText(personnage.getPhysicalDescription());
        txtSaisieDescriptionPersonnage.setText(personnage.getPersonality());

        lblEnteteMode.setText("Modifier le personnage");
        lblEnteteNomHistoire.setText(story.getTitle());

        btnSupprimerPersonnage.setVisible(true);
        btnSupprimerPersonnage.setManaged(true);
    }

    public void initForCreation(Story story) {
        this.modeEdition  = false;
        this.storyParente = story;

        lblEnteteMode.setText("Nouveau personnage");
        lblEnteteNomHistoire.setText(story.getTitle());

        btnSupprimerPersonnage.setVisible(false);
        btnSupprimerPersonnage.setManaged(false);
    }

    @FXML
    private void onBtnSaveCharacter() {
        if (!validateForm()) return;

        String           prenom                 = txtSaisiePrenom.getText().trim();
        String           nom                    = txtSaisieNomPersonnage.getText().trim();
        int              age                    = Integer.parseInt(txtSaisieAge.getText().trim());
        Personnage.Role  role                   = cboSelectionRole.getValue();
        Personnage.Genre genre                  = cboGenre.getValue();
        String           descriptionPhysique    = txtSaisieDescriptionPersonnagePhysique.getText();
        String           descriptionPersonnalite = txtSaisieDescriptionPersonnage.getText();

        try {
            if (modeEdition) {
                personnageEnCours.setFirstName(prenom);
                personnageEnCours.setLastName(nom);
                personnageEnCours.setAge(age);
                personnageEnCours.setRole(role);
                personnageEnCours.setGender(genre);
                personnageEnCours.setPhysicalDescription(descriptionPhysique);
                personnageEnCours.setPersonality(descriptionPersonnalite);
                personnageService.updateCharacter(personnageEnCours);
            } else {
                Personnage personnage = new Personnage();
                personnage.setFirstName(prenom);
                personnage.setLastName(nom);
                personnage.setAge(age);
                personnage.setRole(role);
                personnage.setGender(genre);
                personnage.setPhysicalDescription(descriptionPhysique);
                personnage.setPersonality(descriptionPersonnalite);
                personnageService.addCharacter(personnage, storyParente);
            }
            closeWindow();
        } catch (SQLException e) {
            showError("Impossible d'enregistrer le personnage : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBtnCancelCharacter() {
        closeWindow();
    }

    @FXML
    private void onBtnDeleteCharacter() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer le personnage \"" + personnageEnCours.getLastName() + "\" ?");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            try {
                personnageService.deleteCharacter(personnageEnCours.getId());
                closeWindow();
            } catch (SQLException e) {
                showError("Impossible de supprimer le personnage : " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        boolean valide = true;

        if (txtSaisiePrenom.getText() == null || txtSaisiePrenom.getText().trim().isEmpty()) {
            lblErreurPrenom.setVisible(true);
            lblErreurPrenom.setManaged(true);
            valide = false;
        } else {
            lblErreurPrenom.setVisible(false);
            lblErreurPrenom.setManaged(false);
        }

        if (txtSaisieNomPersonnage.getText() == null || txtSaisieNomPersonnage.getText().trim().isEmpty()) {
            lblErreurNomPersonnage.setVisible(true);
            lblErreurNomPersonnage.setManaged(true);
            valide = false;
        } else {
            lblErreurNomPersonnage.setVisible(false);
            lblErreurNomPersonnage.setManaged(false);
        }

        String ageTexte = txtSaisieAge.getText();
        if (ageTexte == null || ageTexte.trim().isEmpty()) {
            lblErreurAge.setText("L'âge est obligatoire");
            lblErreurAge.setVisible(true);
            lblErreurAge.setManaged(true);
            valide = false;
        } else {
            try {
                int age = Integer.parseInt(ageTexte.trim());
                if (age < 0 || age > 999) {
                    lblErreurAge.setText("L'âge doit être entre 0 et 999");
                    lblErreurAge.setVisible(true);
                    lblErreurAge.setManaged(true);
                    valide = false;
                } else {
                    lblErreurAge.setVisible(false);
                    lblErreurAge.setManaged(false);
                }
            } catch (NumberFormatException e) {
                lblErreurAge.setText("L'âge doit être un nombre");
                lblErreurAge.setVisible(true);
                lblErreurAge.setManaged(true);
                valide = false;
            }
        }

        if (cboSelectionRole.getValue() == null) {
            lblErreurRole.setVisible(true);
            lblErreurRole.setManaged(true);
            valide = false;
        } else {
            lblErreurRole.setVisible(false);
            lblErreurRole.setManaged(false);
        }

        if (cboGenre.getValue() == null) {
            lblErreurRole1.setVisible(true);
            lblErreurRole1.setManaged(true);
            valide = false;
        } else {
            lblErreurRole1.setVisible(false);
            lblErreurRole1.setManaged(false);
        }

        return valide;
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAnnulerPersonnage.getScene().getWindow();
        stage.close();
    }
}