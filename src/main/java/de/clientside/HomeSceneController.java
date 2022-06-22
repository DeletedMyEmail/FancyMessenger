package de.clientside;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

public class HomeSceneController {

    private ClientBackend backend;

    @FXML
    public Text titleText;

    @FXML
    public void initialize()
    {
        backend = SceneController.getBackend();
    }

    @FXML
    public void onAccountButtonClick()
    {
        if (!ClientBackend.getUsername().equals(""))
        {
            SceneController.switchToSettingsScene();
        }
        else
        {
            SceneController.switchToLoginScene();
        }
    }
}
