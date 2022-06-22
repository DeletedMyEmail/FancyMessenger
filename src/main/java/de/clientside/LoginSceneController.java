package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginSceneController {

    private ClientBackend backend;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public void initialize()
    {
        backend = SceneController.getBackend();
    }

    @FXML
    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException
    {
        backend.sendToServer("KMES;login;"+usernameField.getText()+";"+passwordField.getText());
    }

    public void switchToMainScene(ActionEvent actionEvent)
    {
        SceneController.switchToHomeScene();
    }
}
