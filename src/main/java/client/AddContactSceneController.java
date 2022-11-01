package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.io.IOException;

/**
 * Controller for the GUI contact adding scene
 *
 * @version stabel-1.1.1 | last edit: 24.08.2022
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

    /**
     * This method is execcuted when the "Add" button is pressed.<br/>
     * If something was entered in the text field, a user with this name is searched for and added to the contacts.
     *
     * @param actionEvent Event caused by button click
     * */
    public void onAddButtonClick(ActionEvent actionEvent) throws IOException {
        if (!usernameTextfield.getText().equals(""))
        {
            backend.sendToServer("doesUserExist;;"+usernameTextfield.getText());
            SceneManager.closeAddContactWindow();
        }
    }
}
