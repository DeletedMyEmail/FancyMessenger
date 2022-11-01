package client;

import javafx.beans.property.SimpleStringProperty;
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

    private ClientBackend backend;

    @FXML
    public Text usernameText;

    @FXML
    public void initialize() {
        backend = SceneManager.getBackend();
        usernameText.textProperty().bind(backend.getUsername());
    }

    @FXML
    public void onHomeButtonClick(ActionEvent actionEvent)
    {
        SceneManager.switchToHomeScene();
    }

    @FXML
    public void onLogOutButtonClick(ActionEvent actionEvent) throws IOException {
        backend.sendToServer("logout");
    }
}

