package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

/**
 * Controller for the GUI settings scene
 *
 * @version 27.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class SettingsSceneController {

    private ClientBackend backend;

    @FXML
    public Text usernameText;

    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
    }

    protected void changeUsernameText(String pUsername)
    {
        usernameText.setText("Current User: "+pUsername);
    }

    public void switchToMainScene(ActionEvent actionEvent)
    {
        SceneManager.switchToHomeScene();
    }

    public void logOut(ActionEvent actionEvent)
    {
        backend.logOut();
        SceneManager.switchToLoginScene();
    }
}

