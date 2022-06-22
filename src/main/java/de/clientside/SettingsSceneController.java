package de.clientside;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.IOException;

public class SettingsSceneController {

    private ClientBackend backend;

    @FXML
    public Text usernameText;

    @FXML
    public void initialize()
    {
        backend = SceneController.getBackend();
    }

    protected void changeUsernameText(String pUsername)
    {
        usernameText.setText("Current User: "+pUsername);
    }

    public void switchToMainScene(ActionEvent actionEvent)
    {
        SceneController.switchToHomeScene();
    }
}

