package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * Controller for the GUI login scene
 *
 * @version stabel-1.1.1 | last edit: 24.08.2022
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
    public void onLoginButtonClick(ActionEvent actionEvent)
    {
        try {
            backend.sendToServer("login;;"+usernameField.getText()+";;"+passwordField.getText());
        }
        catch (IOException ioEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }

    public void switchToMainScene(ActionEvent actionEvent)
    {
        SceneManager.switchToHomeScene();
    }

    public void onRegisterButtonClick(ActionEvent actionEvent) {
        try {
            backend.sendToServer("register;;"+usernameField.getText()+";;"+passwordField.getText());
        }
        catch (IOException ioEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }
}
