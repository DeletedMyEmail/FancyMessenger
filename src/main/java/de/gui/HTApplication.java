package de.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HTApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HTApplication.class.getResource("kmes_main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 850);
        stage.setTitle("KTrack | Health Tracker by J.H.");
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}