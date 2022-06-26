package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

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

    public void onAddButtonClick(ActionEvent actionEvent)
    {
        if (!usernameTextfield.getText().equals(""))
        {
            backend.addContact(usernameTextfield.getText());
            SceneManager.closeAddContactWindow();
        }
    }
}
