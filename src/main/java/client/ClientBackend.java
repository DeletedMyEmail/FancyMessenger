package client;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import KLibrary.Utils.SQLUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * This class acts as a client backend for the KMes Messenger. <br/>
 * Handles inputs from the KMes Server, manages the local database and controls the GUI via {@link HomeSceneController}
 *
 * @version v3.0.0 | last edit: 16.09.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    // KMes Server Hostname/IP
    private static final String HOST = "localhost";

    // Port of the KMes Server which accepts clients
    private static final int PORT = 4242;

    private final EncryptionUtils encryptionUtils;
    private SQLUtils sqlUtils;

    // Connection to KMes Server
    private Socket server;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private SecretKey AESKey;

    private String currentUser = "";

    /**
     * Creates an instance of this class, iniitializes utility classes and establishes a database connection
     *
     * @see EncryptionUtils
     * @see SQLUtils
     * */
    public ClientBackend() {
        encryptionUtils = new EncryptionUtils();
        try {
            sqlUtils = new SQLUtils("src/main/resources/kmes_client.db");
        } catch (SQLException sqlEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Could not load contacts and history", "Database error", ButtonType.OK);
        }
    }

    /**
     * @return Returns the username of the current user or an empty string if not logged in.
     * */
    protected String getUsername() { return currentUser; }

    public void requestLogout() throws IOException {
        sendToServer("logout");
    }

    /**
     * @return Returns the connection status to the KMes Server
     * */
    protected boolean isConnected()
    {
        return server != null && !server.isClosed();
    }

    /**
     * Updates the current user, the GUI and loads the account settings scene
     *
     * @param pUsername Username of the new logged-in user
     * */
    private void updateCurrentUser(String pUsername) {
        currentUser = pUsername;
        SceneManager.getSettingsScene().changeUsernameText(currentUser);
        SceneManager.switchToSettingsScene();
    }

    /**
     * Adds a new message to the list of messages with a specific user.
     * If there is no such list in the HashMap in conjunction with this user, a new one will be added and displayed.
     *
     * @param pContactName Name of the user who sent/received this message
     * @param pContent Message content
     * */
    protected void addNewMessage(String pContactName, String pContent, boolean pReceived) {
        String lMessage = pContent;
        Extention lFileExtention = Extention.NONE;
        if (pContent.startsWith("[file]")) {
            int lLastBracket = pContent.indexOf(']', 6);
            lFileExtention = Extention.valueOf(pContent.substring(7,lLastBracket).toUpperCase());
            lMessage = lMessage.substring(lLastBracket+1);
        }

        insertContact(pContactName);
        SceneManager.getHomeScene().showNewContact(pContactName);
        SceneManager.getHomeScene().showNewMessage(pContactName, lMessage, lFileExtention, pReceived);
        SceneManager.getHomeScene().showNotification(pContactName);

        try {
            sqlUtils.onExecute(
                    "INSERT INTO Message (Content,Extention) VALUES(?,?);",
                    lMessage, lFileExtention.name());
            sqlUtils.onExecute(
                    "INSERT INTO MessageToContact VALUES( " +
                            "(SELECT ContactID FROM Contact WHERE ContactName= ? AND AccountName= ? ), " +
                            "(SELECT max(MessageID) FROM Message), ?);",
                    pContactName, currentUser, pReceived
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new thread which connects the KMes Server on port {@value PORT}
     * with the address {@value HOST} listens for server inputs and handles them.
     * Method ends if the thread is closed
     *
     * @see java.net.ServerSocket
     * @see Socket
     * @see Thread
     * */
    protected void listenForServerInput () {
        new Thread(() -> {
            try {
                while (true) // do not criticize obvious stupidity
                {
                    // Connects to the KMes Server and iniitializes attributes for communication
                    server = new Socket();
                    server.connect(new InetSocketAddress(HOST, PORT), 4000);
                    outStream = new DataOutputStream(server.getOutputStream());
                    inStream = new DataInputStream(server.getInputStream());

                    establishEncryption();

                    // Handles inputs as long as connected to the KMes Server
                    while (isConnected())
                    {
                        String[] lInput = readFromServer().split(";;");

                        switch (lInput[0]) {
                            case "loggedIn" -> {
                                updateCurrentUser(lInput[1]);
                                loadHistory();
                            }
                            case "loggedOut" -> {
                                currentUser = "";
                                SceneManager.getHomeScene().clearMessagesAndContacts();
                                SceneManager.switchToLoginScene();
                            }
                            case "error" -> SceneManager.showAlert(Alert.AlertType.ERROR, lInput[1], lInput[2], ButtonType.OK);
                            case "message" -> addNewMessage(lInput[1], lInput[2], true);
                            case "userExists" -> {
                                insertContact(lInput[1]);
                                SceneManager.showAlert(Alert.AlertType.CONFIRMATION, "Successfully added" +
                                        " new contact: " + lInput[1], "New contact", ButtonType.OK);
                            }
                        }
                    }
                }
            }
            catch (SocketTimeoutException | SocketException socketException)
            {
                SceneManager.showAlert(Alert.AlertType.ERROR, "",
                        "Connection to the KMesServer couldn't be established",
                        event -> System.exit(0), ButtonType.OK);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    /**
     * Loads all contacts and messages in the database in conjunction with the current user
     *
     * @see SQLUtils
     * */
    private void loadHistory()
    {
        try
        {
            ResultSet lResult = sqlUtils.onQuery("SELECT ContactName FROM Contact WHERE AccountName='"+currentUser+"'");

            while (lResult.next())
                SceneManager.getHomeScene().showNewContact(lResult.getString("ContactName"));

            lResult = sqlUtils.onQuery("SELECT Message.Content, Message.Extention, MessageToContact.SentOrReceived, Contact.ContactName " +
                                                "FROM Message " +
                                                "INNER JOIN MessageToContact " +
                                                "ON MessageToContact.MessageID = Message.MessageID " +
                                                "INNER JOIN Contact " +
                                                "ON Contact.ContactID = MessageToContact.ContactID " +
                                                "WHERE Contact.AccountName='"+currentUser+"'");

            while (lResult.next())
                SceneManager.getHomeScene().showNewMessage(
                        lResult.getString("ContactName"),
                        lResult.getString("Content"),
                        Extention.valueOf(lResult.getString("Extention").toUpperCase()),
                        lResult.getBoolean("SentOrReceived")
                );
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertContact(String pContactName) {
        try {
            if (!sqlUtils.onQuery("SELECT * FROM Contact WHERE ContactName=? AND AccountName=?", pContactName, currentUser).next()) {
                sqlUtils.onExecute("INSERT INTO Contact (ContactName, AccountName) VALUES(?,?)", pContactName, currentUser);
            }
            SceneManager.getHomeScene().showNewContact(pContactName);
        } catch (SQLException sqlEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Couldn't add contact", "Database error", ButtonType.OK);
        }
    }

    /**
     * Sends an encrypted message (AES) to the KMes Server
     *
     * @param pMessage Message to be sent
     *
     * @see #establishEncryption()
     * @see EncryptionUtils
     * */
    protected void sendToServer(String pMessage) throws IOException
    {
        try
        {
            byte[] lEncrpytedMessage = EncryptionUtils.encryptAES(pMessage, AESKey);
            byte[] lMessageSizeAsBytes = ByteBuffer.allocate(4).putInt(lEncrpytedMessage.length).array();
            byte[] lConcatenated = ByteBuffer.allocate(lMessageSizeAsBytes.length+lEncrpytedMessage.length).put(lMessageSizeAsBytes).put(lEncrpytedMessage).array();

            outStream.write(lConcatenated, 0 , lConcatenated.length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Reads and decryptes a message (AES) to the KMes Server
     *
     * @return  Decrypted message as string sent from the KMes Server
     *
     * @see #establishEncryption()
     * @see EncryptionUtils
     * */
    private String readFromServer() throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        int lSize = inStream.readInt();
        byte[] lEncryptedInput = new byte[lSize];
        inStream.readFully(lEncryptedInput);
        return EncryptionUtils.decryptAES(lEncryptedInput, AESKey);
    }

    /**
     * Sends public RSA key to the server and receives the server's public RSA key.
     * Uses private key to decrypt the new AES key which has been encrypted with the client's public key.
     * */
    private void establishEncryption() throws IOException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // RSA
        outStream.write(encryptionUtils.getPublicKey().getEncoded());
        byte[] lKeyBytes = new byte[294];
        inStream.read(lKeyBytes);
        PublicKey lServerRSAKey = EncryptionUtils.decodeRSAKey(lKeyBytes);

        // AES
        AESKey = EncryptionUtils.generateSymmetricKey();
        byte[] lEncryptedAESKey = encryptionUtils.encryptRSA(AESKey.getEncoded(), lServerRSAKey);
        outStream.write(lEncryptedAESKey);
    }

    /**
     * Displays the message in {@link HomeSceneController} encryptes a message and requests to send it to the user pReceiver
     *
     * @param pReceiver     User who'll receive the message
     * @param pMessage      Unencrypted message which will be sent
     *
     * @see #sendToServer(String)
     * @see #addNewMessage(String, String, boolean)
     * */
    public void sendMessageToOtherUser(String pReceiver, String pMessage)
    {
        try
        {
            char[] lMessage = pMessage.toCharArray();
            if (lMessage[0] == ';') lMessage[0] = ',';
            if (lMessage[lMessage.length-1] == ';') lMessage[lMessage.length-1] = ',';

            sendToServer("send;;"+pReceiver+";;"+String.valueOf(lMessage));
            addNewMessage(pReceiver, String.valueOf(lMessage), false);
        }
        catch (IOException ioEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }

    /**
     * Opens a {@link FileChooser} dialog,
     * displays the selected file in {@link HomeSceneController}, converts it to encrypted bytes and requests to send it to the user pReceiver
     *
     * @param pReceiver     User who'll receive the message
     *
     * @see #sendToServer(String)
     * @see #addNewMessage(String, String, boolean)
     * */
    public void sendFile(String pReceiver) {
        FileChooser lChooser = new FileChooser();
        lChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.pdf", "*.txt")
        );
        lChooser.setTitle("Choose a file");
        File lFile = lChooser.showOpenDialog(SceneManager.getStage());
        if (lFile == null) return;

        String lFileExtention = lFile.getName().substring(lFile.getName().lastIndexOf('.')+1);

        try {
            FileInputStream lFileStream = new FileInputStream(lFile);
            byte[] lImageBytes = new byte[(int) lFile.length()];
            lFileStream.read(lImageBytes);
            lFileStream.close();
            sendMessageToOtherUser(pReceiver, "[file]["+lFileExtention+"]"+Base64.getEncoder().encodeToString(lImageBytes));
        }
        catch (IOException ex) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can not convert this file");
        }
    }

    /**
     * Opens a {@link FileChooser} save dialog and saves the image converted to the file extention the selected file has.
     *
     * @param pImage     The image to save
     *
     * @see #sendToServer(String)
     * @see #addNewMessage(String, String, boolean)
     * */
    public void saveFile(Image pImage) {
        FileChooser lChooser = new FileChooser();
        lChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File lFileToSaveTo = lChooser.showSaveDialog(SceneManager.getStage());
        if (lFileToSaveTo == null) SceneManager.showAlert(Alert.AlertType.ERROR, "", "Please select a file");
        else
        {
            try
            {
                lFileToSaveTo.createNewFile();
                BufferedImage lBufferedImage = SwingFXUtils.fromFXImage(pImage, null);
                BufferedImage imageRGB = new BufferedImage(lBufferedImage.getWidth(), lBufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                imageRGB.createGraphics().drawImage(lBufferedImage, 0, 0, null);

                String lFileExtension = lFileToSaveTo.getName();
                lFileExtension = lFileExtension.substring(lFileExtension.lastIndexOf('.')+1);

                ImageIO.write(imageRGB, lFileExtension, lFileToSaveTo);
            }
            catch (IOException ioEx) {
                SceneManager.showAlert(Alert.AlertType.ERROR, "", "Error occured while saving");
            }
            catch (SecurityException secEx) {
                SceneManager.showAlert(Alert.AlertType.ERROR, "", "Access denied");
            }
        }
    }
}
