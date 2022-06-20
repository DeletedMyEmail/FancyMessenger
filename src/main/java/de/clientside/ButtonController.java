package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ButtonController {

    private static Stage stage;
    private static Scene scene;
    private static Parent root;
    private static boolean logedIn = false;

    @FXML
    private Text usernameText;

    protected static void setLoginState(boolean bool)
    {
        logedIn = bool;
    }

    public void switchToAccScene(ActionEvent event) throws IOException {
        String filename;
        if (logedIn)
        {
            filename = "settings_scene.fxml";
        }
        else
        {
            filename = "login_scene.fxml";
        }

        root = FXMLLoader.load(ButtonController.class.getResource(filename));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        if (logedIn)
        {
            usernameText.setText("Current User"+ClientBackend.getUsername());
        }
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

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException {
        ClientBackend.sendToServer("KMES;login;"+usernameField+";"+passwordField);
    }

}
