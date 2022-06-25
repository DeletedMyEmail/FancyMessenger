package de.clientside;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

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
    public VBox message_box;

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

    protected void showNewMessage(String author, String message)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                String cssLayout = "-fx-border-color: #6bc490";
                final HBox hbox = new HBox();
                hbox.setMaxWidth(350.0);
                hbox.getChildren().add(new Text("**"+author+"**: "+message));
                hbox.setStyle(cssLayout);
                message_box.getChildren().addAll(hbox);
            }
        });
    }

    public void onSendButtonClick(ActionEvent actionEvent) throws IOException {
        backend.sendToServer("KMES;send;Admin;Test");
    }
}
