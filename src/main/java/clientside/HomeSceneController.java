package clientside;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Controller for the GUI home scene
 *
 * @version 27.06.2022
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

    private void showAllMessages(Text pText) {
        ObservableList<Node> messageList = ((VBox)messagesScrollpane.getContent()).getChildren();
        showAllMessages(pText, messageList);
    }

    private void showAllMessages(Text pText, ObservableList pMessageList)
    {
        if (backend.getMessagesForUser(pText.getText()) != null)
        {
            for (String msg : backend.getMessagesForUser(pText.getText()))
            {
                pMessageList.add(createMessageHBox(msg));
            }
            System.out.println("New messages added");
        }
    }

    private void clearCurrentMessages() {
        ObservableList messageList = ((VBox)messagesScrollpane.getContent()).getChildren();
        messageList.clear();
    }

    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
        contactsList.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                clearCurrentMessages();
                showAllMessages((Text)t1);
            }
        }));
    }

    @FXML
    protected void onAccountButtonClick()
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

    @FXML
    protected void onSendButtonClick(ActionEvent actionEvent) throws IOException
    {
        Text selectedContact = ((Text)contactsList.getSelectionModel().getSelectedItem());
        if (selectedContact == null) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        String receiver = selectedContact.getText();
        String msg = messageTextField.getText();
        backend.addNewMessage(receiver, "Sent: "+msg);
        backend.sendToServer("KMES;send;"+receiver+";"+msg);
    }

    @FXML
    protected void onAddContactButtonClick(ActionEvent actionEvent)
    {
        SceneManager.showAddContactWindow();
    }

    private void clearContacts()
    {
        contactsList.getSelectionModel().clearSelection();
        contactsList.getItems().clear();
    }

    protected void clearShowMessagesAndContacts()
    {
        clearCurrentMessages();
        clearContacts();
    }
}
