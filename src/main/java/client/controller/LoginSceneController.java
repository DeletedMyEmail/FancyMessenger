package client.controller;

import client.ServerController;
import client.model.SceneAndControllerModel;
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
public class LoginSceneController extends ControllerUtils {

    private ServerController serverController;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public Button registerButton;

    public LoginSceneController() {
        serverController = ServerController.getInstance();
    }

    @FXML
    public void onLoginButtonClick(ActionEvent actionEvent) {
        try {
            serverController.sendToServer("login;;"+usernameField.getText()+";;"+passwordField.getText());
        }
        catch (IOException ioEx) {
            showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }

    @FXML
    public void onHomeClick(ActionEvent actionEvent) {
        SceneAndControllerModel lModel = SceneAndControllerModel.getInstance();
        lModel.getMainStage().setScene(lModel.getHomeScene());
    }

    public void onRegisterButtonClick(ActionEvent actionEvent) {
        try {
            serverController.sendToServer("register;;"+usernameField.getText()+";;"+passwordField.getText());
        }
        catch (IOException ioEx) {
            showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }
}
