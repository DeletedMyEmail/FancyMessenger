package de.clientside;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GUIInitializer extends Application
{

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("kmes_main.fxml")));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(GUIInitializer.class.getResource("styles.css")).toExternalForm());

        stage.setTitle("Kmes | Messenger by J.H.");
        stage.setScene(scene);
        stage.show();
    }
}