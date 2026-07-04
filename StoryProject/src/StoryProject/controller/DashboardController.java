package StoryProject.controller;

import StoryProject.model.StatistiquesGlobales;
import StoryProject.repository.DBManager;
import StoryProject.repository.PersonnageRepository;
import StoryProject.repository.SceneRepository;
import StoryProject.repository.StoryRepository;
import StoryProject.service.GlobalStatsService;
import StoryProject.service.PersonnageService;
import StoryProject.service.SceneService;
import StoryProject.service.StoryService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

/** Contrôleur de la page "Statistiques" : remplit les indicateurs et les graphiques. */
public class DashboardController implements Initializable {

    @FXML private Label lblNbHistoires;
    @FXML private Label lblNbPersonnages;
    @FXML private Label lblNbScenes;
    @FXML private Label lblPctTermine;

    @FXML private BarChart<String, Number> chartScenesParHistoire;
    @FXML private PieChart                  chartStatuts;
    @FXML private BarChart<String, Number> chartRoles;
    @FXML private BarChart<String, Number> chartTags;

    @FXML private Button btnRetour;

    private GlobalStatsService statsService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statsService = new GlobalStatsService(
                new StoryService(new StoryRepository(DBManager.getInstance())),
                new SceneService(new SceneRepository(DBManager.getInstance()),
                                 new PersonnageRepository(DBManager.getInstance())),
                new PersonnageService(new PersonnageRepository(DBManager.getInstance())));
        load();
    }

    private void load() {
        try {
            StatistiquesGlobales s = statsService.compute();

            // Indicateurs clés
            lblNbHistoires.setText(String.valueOf(s.storyCount()));
            lblNbPersonnages.setText(String.valueOf(s.characterCount()));
            lblNbScenes.setText(String.valueOf(s.sceneCount()));
            lblPctTermine.setText(s.completionPercentage() + " %");

            // Scènes par histoire (barres)
            XYChart.Series<String, Number> serieScenes = new XYChart.Series<>();
            for (Map.Entry<String, Integer> e : s.scenesByStory().entrySet()) {
                serieScenes.getData().add(new XYChart.Data<>(shorten(e.getKey()), e.getValue()));
            }
            chartScenesParHistoire.getData().setAll(serieScenes);

            // Statuts des scènes (camembert)
            chartStatuts.getData().clear();
            s.statusDistribution().forEach((statut, n) ->
                    chartStatuts.getData().add(new PieChart.Data(statut.getLabel() + " (" + n + ")", n.doubleValue())));

            // Personnages par rôle (barres)
            XYChart.Series<String, Number> serieRoles = new XYChart.Series<>();
            s.charactersByRole().forEach((role, n) ->
                    serieRoles.getData().add(new XYChart.Data<>(role.name(), n)));
            chartRoles.getData().setAll(serieRoles);

            // Tags les plus utilisés (barres)
            XYChart.Series<String, Number> serieTags = new XYChart.Series<>();
            s.topTags().forEach((tag, n) ->
                    serieTags.getData().add(new XYChart.Data<>(tag, n)));
            chartTags.getData().setAll(serieTags);

        } catch (SQLException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger les statistiques : " + e.getMessage());
            alert.showAndWait();
        }
    }

    /** Raccourcit un titre trop long pour rester lisible sous une barre. */
    private String shorten(String titre) {
        if (titre == null) return "";
        return titre.length() > 14 ? titre.substring(0, 13) + "…" : titre;
    }

    @FXML
    private void onBtnBack() {
        ((Stage) btnRetour.getScene().getWindow()).close();
    }
}
