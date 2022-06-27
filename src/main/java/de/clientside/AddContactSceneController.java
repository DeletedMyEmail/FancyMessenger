package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Controller for GUI contact adding scene
 *
 * @version 27.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class AddContactSceneController
{
    private ClientBackend backend;

    @FXML
    public TextField usernameTextfield;

    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
    }

    public void onAddButtonClick(ActionEvent actionEvent) throws IOException {
        if (!usernameTextfield.getText().equals(""))
        {
            backend.sendToServer("KMES;doesUserExist;"+usernameTextfield.getText());
            SceneManager.closeAddContactWindow();
        }
    }
}
