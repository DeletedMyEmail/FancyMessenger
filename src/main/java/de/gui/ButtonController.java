package de.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ButtonController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private boolean logedIn = false;

    public void switchToAccScene(ActionEvent event) throws IOException {
        String filename;
        if (logedIn)
        {
            filename = "acc_scene.fxml";
        }
        else
        {
            filename = "login_scene.fxml";
        }

        root = FXMLLoader.load(getClass().getResource(filename));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToMainScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("kmes_main.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void onAccountButtonClick(ActionEvent actionEvent) {

    }

    public void onLoginButtonClick(ActionEvent actionEvent) {

    }

}
