package client.controller;

import client.ServerController;
import client.model.SceneAndControllerModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Controller for the GUI settings scene
 *
 * @version stabel-1.1.1 | last edit: 01.11.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class SettingsSceneController {

    private final ServerController serverController;

    public SettingsSceneController() {
        serverController = ServerController.getInstance();
    }

    @FXML
    public Text usernameText;

    @FXML
    public void initialize() {
        usernameText.textProperty().bind(serverController.getUsername());
    }

    @FXML
    public void onHomeButtonClick(ActionEvent actionEvent) {
        SceneAndControllerModel lModel = SceneAndControllerModel.getInstance();
        lModel.getMainStage().setScene(lModel.getLoginScene());
    }

    @FXML
    public void onLogOutButtonClick(ActionEvent actionEvent) throws IOException {
        serverController.sendToServer("logout");
    }
}

