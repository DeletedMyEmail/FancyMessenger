package client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Controller for the GUI home scene
 *
 * @version v2.0.0 | last edit: 24.08.2022
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
        showAllMessages(pText.getText(), messageList);
    }

    private void showAllMessages(String pContact, ObservableList pMessageList)
    {
        if (backend.getMessagesForUser(pContact) != null)
        {
            for (String msg : backend.getMessagesForUser(pContact))
            {
                if (msg.startsWith("Received: ")) pMessageList.add(createMessageBox(msg.substring(10), true));
                else pMessageList.add(createMessageBox(msg.substring(6), false));
            }
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

    private VBox createMessageBox(String pContent, boolean pReceiving)
    {
        VBox lVBox = new VBox();
        lVBox.setMaxWidth(800);
        lVBox.setMaxHeight(400);
        TextFlow lTextflow = new TextFlow();
        Text lSentOrReceived = new Text();


        if (pReceiving) {
            lVBox.setStyle("-fx-background-color: #c7c9c7;");
            lSentOrReceived.setText("Received: ");
        }
        else {
            lVBox.setStyle("-fx-background-color: #d6f4cd;");
            lSentOrReceived.setText("Sent: ");
        }
        lSentOrReceived.setFont(Font.font("System", FontWeight.BOLD, 15));
        lTextflow.getChildren().add(lSentOrReceived);

        if (pContent.startsWith("[image]")) {
            byte[] lImageBytes = Base64.getDecoder().decode(pContent.substring(7));
            Image lImg = new Image(new ByteArrayInputStream(lImageBytes));
            ImageView lImgView = new ImageView(lImg);
            lImgView.setCache(true);
            lImgView.setPreserveRatio(true);
            lImgView.setFitHeight(400);
            lImgView.setFitWidth(600);
            lTextflow.getChildren().add(lImgView);
        }
        else {
            Text lText = new Text();
            lText.setFont(Font.font("System", FontWeight.NORMAL, 14));
            lText.setText(pContent);
            lTextflow.getChildren().add(lText);
        }
        lVBox.getChildren().add(lTextflow);
        return lVBox;
    }

    protected void showNewMessageIfChatActive(String pAuthor, String pMessage, boolean pReceived)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                Text item = ((Text)contactsList.getSelectionModel().getSelectedItem());

                if (item != null && item.getText().equals(pAuthor))
                {
                    ((VBox)messagesScrollpane.getContent()).getChildren().add(createMessageBox(pMessage, pReceived));
                }
            }
        });
    }

    protected void showNewContact(String pUsername)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                contactsList.getItems().add(new Text(pUsername));
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

        String lReceiver = selectedContact.getText();
        String lMsg = messageTextField.getText();
        backend.sendMessageToOtherUser(lReceiver, lMsg);
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

    @FXML
    public void onFileButtonClick(ActionEvent actionEvent) {
        Text selectedContact = ((Text)contactsList.getSelectionModel().getSelectedItem());
        if (selectedContact == null) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        String lReceiver = selectedContact.getText();
        backend.fileButtonClick(lReceiver);
    }
}
