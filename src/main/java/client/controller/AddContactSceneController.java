package client.controller;

import client.ServerController;
import client.model.SceneAndControllerModel;
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
public class AddContactSceneController {

    @FXML
    public TextField usernameTextfield;

    @FXML
    public void initialize() {

    }

    public void onAddButtonClick(ActionEvent pActionEvent) throws IOException {
        if (!usernameTextfield.getText().isEmpty()) {
            ServerController.getInstance().sendToServer("doesUserExist;;"+usernameTextfield.getText());
            SceneAndControllerModel.getInstance().getAddContactStage().close();
        }
    }
}
