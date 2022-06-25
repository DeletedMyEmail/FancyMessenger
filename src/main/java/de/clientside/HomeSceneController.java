package de.clientside;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

/**
 * Controller for the GUI home scene
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class HomeSceneController {

    private ClientBackend backend;

    @FXML
    public Text titleText;

    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
    }

    @FXML
    public void onAccountButtonClick()
    {
        if (!backend.getUsername().equals(""))
        {
            SceneManager.switchToSettingsScene();
        }
        else
        {
            SceneManager.switchToLoginScene();
        }
    }
}
