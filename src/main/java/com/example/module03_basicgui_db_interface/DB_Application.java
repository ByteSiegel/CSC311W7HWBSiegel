package com.example.module03_basicgui_db_interface;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The main application class that initializes the application stages and scenes.
 */
public class DB_Application extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        DB_Application.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        showSplashScreen();
    }

    /**
     * Displays the splash screen with a fade-out transition.
     */
    private void showSplashScreen() {
        try {
            // Load the splash screen FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("splash_screen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 850, 560);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Set up a fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                // After the fade-out, load the main application scene directly
                showMainScene();
            });

            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the main application scene.
     */
    public void showMainScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("db_interface_gui.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 850, 560);
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Management");
            scene.getStylesheets().add(getClass().getResource("/com/example/module03_basicgui_db_interface/light-theme.css").toExternalForm());
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point of the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
