package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

/**
 * Controller for the GUI login scene
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class LoginSceneController {

    private ClientBackend backend;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public Button registerButton;
    
    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
    }

    @FXML
    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException
    {
        backend.sendToServer("KMES;login;"+usernameField.getText()+";"+passwordField.getText());
    }

    public void switchToMainScene(ActionEvent actionEvent)
    {
        SceneManager.switchToHomeScene();
    }

    public void onRegisterButtonClick(ActionEvent actionEvent) throws IOException {
        backend.sendToServer("KMES;register;"+usernameField.getText()+";"+passwordField.getText());
    }
}
