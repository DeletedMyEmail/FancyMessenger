package client;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * Client backend for KMes Messenger<br/>
 * Handles input from the KMes Server
 *
 * @version v2.0.1 | last edit: 27.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    // Hostname/IP from KMes Server
    private static final String HOST = "localhost";

    // Port of the KMes Server which accepts clients
    private static final int PORT = 4242;

    private final HashMap<String, List<String>> messages;
    private final EncryptionUtils encryptionUtils;

    // connection to KMes Server
    private Socket server;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private SecretKey AESKey;

    // private BufferedReader reader;
    // private BufferedWriter writer;

    private String currentUser = "";

    /**
     * Creates a ClientBackend instance, iniitializes attributes and transfers old saved messages into a HashMap
     * in conjunction with the receiver/sender
     * */
    public ClientBackend() {
        messages = new HashMap<>();
        encryptionUtils = new EncryptionUtils();
        /*
        reader = new BufferedReader(new FileReader("src/main/contacts.txt"));
        writer = new BufferedWriter(new FileWriter("src/main/contacts.txt"));

        contacts = new ArrayList<>();
        String in;
        while((in = reader.readLine()) != null)
        {
            //TODO
        }*/
    }

    /**
     * @param pUsername Name of the user whose sent/received messages are to be returned
     * @return Returns a list of strings including all sent messages with the specified user
     * */
    protected List<String> getMessagesForUser(String pUsername)
    {
        return messages.get(pUsername);
    }

    /**
     * @return Returns the username from the current user or an empty string if not logged in.
     * */
    protected String getUsername() { return currentUser; }

    /**
     * Logs out the current user.<br/>
     * Both server and client will no longer associate a specific user with this socket.
     * */
    protected void logOut() {
        try
        {
            sendToServer("logout");
            messages.clear();
            SceneManager.getHomeScene().clearShowMessagesAndContacts();
            currentUser = "";
        }
        catch (IOException ex)
        {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMesServer", ButtonType.OK);
        }
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
     * If there is no such list in the HashMap in conjunction with this user, a new one will be added.
     *
     * @param pUsername Name of the user who sent/received this message
     * @param pMessage Message content
     * */
    protected void addNewMessage(String pUsername, String pMessage)
    {
        if (messages.get(pUsername) == null)
        {
            messages.put(pUsername, new ArrayList<>() {{
                add(pMessage);
            }});
            SceneManager.getHomeScene().showNewContact(pUsername);
        }
        else
        {
            messages.get(pUsername).add(pMessage);
        }
        SceneManager.getHomeScene().showNewMessage(pUsername, pMessage);
    }

    /**
     * Creates a new Thread which listens for server inputs and handles them
     * */
    protected void listenForServerInput () {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    while (true) // do not criticize obvious stupidity
                    {
                        // Connects to the KMes Server and iniitializes attributes for communication
                        server = new Socket();
                        server.connect(new InetSocketAddress(HOST, PORT), 4000);
                        outStream = new DataOutputStream(server.getOutputStream());
                        inStream = new DataInputStream(server.getInputStream());

                        establishEncryption();

                        // Handles inputs as long as the connection exists
                        while (isConnected())
                        {
                            String[] lInput = readFromServer().split(";;");

                            switch (lInput[0]) {
                                case "loggedIn" -> updateCurrentUser(lInput[1]);
                                case "error" -> SceneManager.showAlert(Alert.AlertType.ERROR, lInput[1], lInput[2], ButtonType.OK);
                                case "message" -> addNewMessage(lInput[1], "Received: " + lInput[2]);
                                case "userExists" -> {
                                    messages.computeIfAbsent(lInput[1], k -> new ArrayList<>());
                                    SceneManager.getHomeScene().showNewContact(lInput[1]);
                                    SceneManager.showAlert(Alert.AlertType.CONFIRMATION, "Successfully added" +
                                            " new contact: " + lInput[1], "New contact", ButtonType.OK);
                                }
                            }
                        }
                    }
                }
                catch (SocketTimeoutException | SocketException socketException)
                {
                    socketException.printStackTrace();
                    SceneManager.showAlert(Alert.AlertType.ERROR, "",
                            "Connection to the KMesServer couldn't be established",
                            event -> System.exit(0), ButtonType.OK);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * Sends a string to the KMes Server
     *
     * @param pMessage Message to be sent
     * */
    protected void sendToServer(String pMessage) throws IOException
    {
        try {
            outStream.writeUTF(EncryptionUtils.encryptAES(pMessage, AESKey));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String readFromServer() throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        String lEncryptedInput = inStream.readUTF();
        return EncryptionUtils.decryptAES(lEncryptedInput, AESKey);
    }

    private void establishEncryption() throws IOException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // RSA
        outStream.write(encryptionUtils.getPublicKey().getEncoded());
        byte[] lKeyBytes = new byte[294];
        inStream.read(lKeyBytes);
        PublicKey lServerRSAKey = EncryptionUtils.decodeRSAKey(lKeyBytes);

        // AES
        AESKey = EncryptionUtils.generateSymmetricKey();
        byte[] lEncryptedAESKey = encryptionUtils.encryptRSA(EncryptionUtils.encodeKey(AESKey), lServerRSAKey);
        outStream.write(lEncryptedAESKey);
    }

    public void sendMessageToOtherUser(String pReceiver, String pMessage)
    {
        try
        {
            sendToServer("send;;"+pReceiver+";;"+pMessage.replace(";;",";"));
            addNewMessage(pReceiver, "Sent: "+pMessage.replace(";;",";"));
        }
        catch (UTFDataFormatException sizeEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Input is too large", ButtonType.OK);
        }
        catch (IOException ioEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }

    public void fileButtonClick(String pReceiver) {
        FileChooser lChooser = new FileChooser();
        lChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        lChooser.setTitle("Choose an image");
        File lFile = lChooser.showOpenDialog(SceneManager.getStage());

        if (lFile == null) return;

        try {
            FileInputStream lFileStream = new FileInputStream(lFile);
            byte[] lImageBytes = new byte[(int) lFile.length()];
            lFileStream.read(lImageBytes);
            lFileStream.close();
            sendMessageToOtherUser(pReceiver, "[image]"+Base64.getEncoder().encodeToString(lImageBytes));
        }
        catch (IOException ex) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can not convert this file");
        }
    }
}
