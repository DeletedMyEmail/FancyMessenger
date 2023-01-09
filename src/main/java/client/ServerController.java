package client;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.utils.EncryptionUtils;
import KLibrary.utils.SQLUtils;

import KLibrary.utils.SystemUtils;
import client.controller.HomeSceneController;
import client.model.SceneAndControllerModel;
import javafx.beans.property.SimpleStringProperty;
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
import java.net.*;
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
 * @version stabel-1.1.1 | last edit: 01.11.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ServerController {

    private static final String SERVER_IP = "134.122.74.216";
    private static final int PORT = 4242;

    private static ServerController instance;

    private final SimpleStringProperty currentUser;
    private final EncryptionUtils encryptionUtils;
    private final SceneAndControllerModel sceneAndControllerModel;

    private SQLUtils sqlUtils;
    private Socket server;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private SecretKey AESKey;

    /**
     * Creates an instance of this class, iniitializes utility classes and establishes a database connection
     *
     * @see EncryptionUtils
     * @see SQLUtils
     * */
    private ServerController() {
        sceneAndControllerModel = SceneAndControllerModel.getInstance();
        currentUser = new SimpleStringProperty("");
        encryptionUtils = new EncryptionUtils();
        try {
            String lKMesDirPath = SystemUtils.getLocalApplicationPath()+"/KMes";
            SystemUtils.createDirIfNotExists(lKMesDirPath);
            sqlUtils = new SQLUtils(lKMesDirPath + "/kmes_client.db");
            createTables();
        }
        catch (SQLException sqlEx) {
            sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "Could not load contacts and history", "Database error", ButtonType.OK);
        }
    }

    public static ServerController getInstance() {
        if (instance == null)
            instance = new ServerController();
        return instance;
    }

    /**
     * Creates a new thread which connects the KMes Server on port {@value PORT}
     * with the address {@value SERVER_IP} listens for server inputs and handles them.
     * Method ends if the thread is closed
     *
     * @see java.net.ServerSocket
     * @see Socket
     * @see Thread
     * */
    public void listenForServerInput() {
        new Thread(() -> {
            try {
                while (true) // do not criticize obvious stupidity
                {
                    // Connects to the KMes Server and iniitializes attributes for communication
                    server = new Socket();
                    server.connect(new InetSocketAddress(SERVER_IP, PORT), 4000);
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
                                currentUser.set("");
                                sceneAndControllerModel.getHomeSceneController().clearMessagesAndContacts();
                                sceneAndControllerModel.getMainStage().setScene(sceneAndControllerModel.getLoginScene());
                            }
                            case "error" -> sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, lInput[1], lInput[2], ButtonType.OK);
                            case "message" -> addNewMessage(lInput[1], lInput[2], true);
                            case "userExists" -> {
                                insertContact(lInput[1]);
                                sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.CONFIRMATION, "Successfully added" +
                                        " new contact: " + lInput[1], "New contact", ButtonType.OK);
                            }
                        }
                    }
                }
            }
            catch (SocketTimeoutException | SocketException socketException)
            {
                sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "",
                        "Connection to the KMesServer couldn't be established",
                        event -> System.exit(0), ButtonType.OK);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    /**
     * Updates the current user, the GUI and loads the account settings scene
     *
     * @param pUsername Username of the new logged-in user
     * */
    private void updateCurrentUser(String pUsername) {
        currentUser.set(pUsername);
        sceneAndControllerModel.getMainStage().setScene(sceneAndControllerModel.getSettingsScene());
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
        sceneAndControllerModel.getHomeSceneController().showNewContact(pContactName);
        sceneAndControllerModel.getHomeSceneController().showNewMessage(pContactName, lMessage, lFileExtention, pReceived);
        sceneAndControllerModel.getHomeSceneController().showNotification(pContactName);

        try {
            sqlUtils.onExecute(
                    "INSERT INTO Message (Content,Extention) VALUES(?,?);",
                    lMessage, lFileExtention.name());
            sqlUtils.onExecute(
                    "INSERT INTO MessageToContact VALUES(" +
                            "(SELECT ContactID FROM Contact WHERE ContactName = ? AND AccountName = ? )," +
                            "(SELECT max(MessageID) FROM Message), ?);",
                    pContactName, currentUser, pReceived
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

            while (!lResult.isClosed() && lResult.next())
                sceneAndControllerModel.getHomeSceneController().showNewContact(lResult.getString("ContactName"));

            lResult = sqlUtils.onQuery("SELECT Message.Content, Message.Extention, MessageToContact.SentOrReceived, Contact.ContactName " +
                                                "FROM Message " +
                                                "INNER JOIN MessageToContact " +
                                                "ON MessageToContact.MessageID = Message.MessageID " +
                                                "INNER JOIN Contact " +
                                                "ON Contact.ContactID = MessageToContact.ContactID " +
                                                "WHERE Contact.AccountName='"+currentUser+"'");

            while (!lResult.isClosed() && lResult.next()) {
                sceneAndControllerModel.getHomeSceneController().showNewMessage(
                        lResult.getString("ContactName"),
                        lResult.getString("Content"),
                        Extention.valueOf(lResult.getString("Extention").toUpperCase()),
                        lResult.getBoolean("SentOrReceived")
                );
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertContact(String pContactName) {
        try
        {
            ResultSet lResultSet = sqlUtils.onQuery("SELECT * FROM Contact WHERE ContactName=? AND AccountName=?", pContactName, currentUser);
            if (lResultSet.isClosed() || !lResultSet.next()) {
                sqlUtils.onExecute("INSERT INTO Contact (ContactName, AccountName) VALUES(?,?)", pContactName, currentUser);
            }
            sceneAndControllerModel.getHomeSceneController().showNewContact(pContactName);
        } catch (SQLException sqlEx) {
            sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "Couldn't add contact", "Database error", ButtonType.OK);
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
    public void sendToServer(String pMessage) throws IOException {
        try {
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
    private void establishEncryption() throws IOException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
    {
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
            sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
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
    public void sendFile(File pFile, String pReceiver) {
        if (pFile == null) return;

        String lFileExtention = pFile.getName().substring(pFile.getName().lastIndexOf('.')+1);

        try {
            FileInputStream lFileStream = new FileInputStream(pFile);
            byte[] lImageBytes = new byte[(int) pFile.length()];
            lFileStream.read(lImageBytes);
            lFileStream.close();
            sendMessageToOtherUser(pReceiver, "[file]["+lFileExtention+"]"+Base64.getEncoder().encodeToString(lImageBytes));
        }
        catch (IOException ex) {
            sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "", "Can not convert this file");
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
        File lFileToSaveTo = lChooser.showSaveDialog(sceneAndControllerModel.getMainStage());
        if (lFileToSaveTo == null) {
            sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "", "Please select a file");
        }
        else {
            try {
                lFileToSaveTo.createNewFile();
                BufferedImage lBufferedImage = SwingFXUtils.fromFXImage(pImage, null);
                BufferedImage imageRGB = new BufferedImage(lBufferedImage.getWidth(), lBufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                imageRGB.createGraphics().drawImage(lBufferedImage, 0, 0, null);

                String lFileExtension = lFileToSaveTo.getName();
                lFileExtension = lFileExtension.substring(lFileExtension.lastIndexOf('.')+1);

                ImageIO.write(imageRGB, lFileExtension, lFileToSaveTo);
            }
            catch (IOException ioEx) {
                sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "", "Error occured while saving");
            }
            catch (SecurityException secEx) {
                sceneAndControllerModel.getHomeSceneController().showAlert(Alert.AlertType.ERROR, "", "Access denied");
            }
        }
    }

    private void createTables() throws SQLException {
        sqlUtils.onExecute("""
                    create table if not exists Contact
                    (
                        ContactName TEXT not null,
                        AccountName TEXT not null,
                        ContactID   INTEGER
                            primary key
                    );
                """);
        sqlUtils.onExecute("""
                    create table if not exists Message
                    (
                        MessageID INTEGER not null
                            primary key,
                        Extention TEXT,
                        Content   BLOB    not null
                    );
                """);
        sqlUtils.onExecute("""
                    create table if not exists MessageToContact
                    (
                        ContactID      INTEGER
                            references Contact,
                        MessageID      INTEGER
                            references Message,
                        SentOrReceived TEXT,
                        primary key (ContactID, MessageID)
                    );
                """);
    };

    /**
     * @return Returns the connection status to the KMes Server
     * */
    public boolean isConnected()
    {
        return server != null && !server.isClosed();
    }

    /**
     * @return Returns the username of the current user or an empty string if not logged in.
     * */
    public SimpleStringProperty getUsername() { return currentUser; }
}
