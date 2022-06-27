package de.clientside;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Iterator;

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
    public TextField messageTextField;

    @FXML
    public ListView contactsList;

    @FXML
    public ScrollPane messagesScrollpane;

    @FXML
    public void initialize()
    {
        //contactsScrollpane.setPannable(true);
        backend = SceneManager.getBackend();
        contactsList.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                Iterator iter;
                ObservableList messagesVBox = ((VBox)messagesScrollpane.getContent()).getChildren();
                while ((iter = messagesVBox.iterator()).hasNext())
                {
                    messagesVBox.remove(iter.next());
                }

                if (backend.getMessagesForUser(((Text)t1).getText()) != null)
                {
                    for (String msg : backend.getMessagesForUser(((Text)t1).getText()))
                    {
                        messagesVBox.add(createMessageHBox(msg));
                    }
                    System.out.println("New messages added");
                }
                else
                {
                    System.out.println("No messages available");
                }
            }
        }));
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

    private HBox createMessageHBox(String content)
    {
        String cssLayout = "-fx-border-color: #6bc490";
        HBox hbox = new HBox();
        hbox.setMaxWidth(350.0);
        hbox.getChildren().add(new Text(content));
        hbox.setStyle(cssLayout);
        return hbox;
    }

    protected void showNewMessage(String author, String message)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                Text item = ((Text)contactsList.getSelectionModel().getSelectedItem());
                if (item != null && item.getText().equals(author))
                {
                    ((VBox)messagesScrollpane.getContent()).getChildren().add(createMessageHBox(message));
                }
            }
        });
    }

    protected void showNewContact(String username)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                contactsList.getItems().add(new Text(username));
            }
        });
    }

    public void onSendButtonClick(ActionEvent actionEvent) throws IOException
    {
        String receiver = ((Text)contactsList.getSelectionModel().getSelectedItem()).getText();
        String msg = messageTextField.getText();
        backend.addNewMessage(receiver, "**Sent**: "+msg);
        backend.sendToServer("KMES;send;"+receiver+";"+msg);
    }

    public void onAddContactButtonClick(ActionEvent actionEvent)
    {
        SceneManager.showAddContactWindow();
    }
}
