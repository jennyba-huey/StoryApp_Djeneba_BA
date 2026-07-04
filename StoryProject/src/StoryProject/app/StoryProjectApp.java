package StoryProject.app;

import StoryProject.repository.DBManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StoryProjectApp extends Application {

    private static final String TITRE_APPLICATION = "StoryProject";
    private static final double LARGEUR_FENETRE   = 1100;
    private static final double HAUTEUR_FENETRE   = 700;
    private static final double LARGEUR_MIN       = 900;
    private static final double HAUTEUR_MIN       = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/StoryProject/view/main.fxml"));
        Scene scene = new Scene(root, LARGEUR_FENETRE, HAUTEUR_FENETRE);

        primaryStage.setTitle(TITRE_APPLICATION);
        primaryStage.setMinWidth(LARGEUR_MIN);
        primaryStage.setMinHeight(HAUTEUR_MIN);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DBManager.getInstance().closeConnection();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}