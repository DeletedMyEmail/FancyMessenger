package client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Client backend for KMes Messenger<br/>
 * Handles input from the KMes server
 *
 * @version 27.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    // Hostname/IP from KMes server
    private final String host = "localhost";
    // Port of the KMes server which accepts clients
    private final int port = 3141;

    // connection to KMes
    private Socket server;
    private DataInputStream input;
    private DataOutputStream output;

    private BufferedReader reader;
    private BufferedWriter writer;

    private List<String> contacts;
    private HashMap<String, List<String>> messages;

    private static String currentUser = "";

    /**
     * Creates a ClientBackend instance, iniitializes attributes and transfers old saved messages into a HashMap
     * in conjunction with the receiver/sender
     * */
    public ClientBackend() throws IOException {
        messages = new HashMap<>();
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
            sendToServer("KMES;logout");
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
     * Sends a string to the KMes server
     *
     * @param pMessage Message to be sent
     * */
    protected void sendToServer(String pMessage) throws IOException
    {
        output.writeUTF(pMessage);
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
                        // Connects to the KMes server and iniitializes attributes for communication
                        server = new Socket();
                        server.connect(new InetSocketAddress(host, port), 3000);
                        output = new DataOutputStream(server.getOutputStream());
                        input = new DataInputStream(server.getInputStream());

                        establishRSA();

                        // Handles inputs as long as the connection exists
                        while (isConnected())
                        {
                            String[] input_str = input.readUTF().split(";");
                            if (input_str[0].equals("KMES"))
                            {
                                switch (input_str[1]) {
                                    case "loggedIn" -> updateCurrentUser(input_str[2]);
                                    case "error" -> SceneManager.showAlert(Alert.AlertType.ERROR, input_str[2], input_str[3], ButtonType.OK);
                                    case "message" -> addNewMessage(input_str[2], "Received: " + input_str[3]);
                                    case "userExists" -> {
                                        messages.computeIfAbsent(input_str[2], k -> new ArrayList<>());
                                        SceneManager.getHomeScene().showNewContact(input_str[2]);
                                        SceneManager.showAlert(Alert.AlertType.CONFIRMATION, "Successfully added" +
                                                " new contact: " + input_str[2], "New contact", ButtonType.OK);
                                    }
                                }
                            }
                            else
                            {
                                server.close();
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
            }
        }).start();

    }

    private void establishRSA() {
    }

    public void sendMessage(String pReceiver, String pMessage)
    {
        try
        {
            sendToServer("KMES;send;"+pReceiver+";"+pMessage);
            addNewMessage(pReceiver, "Sent: "+pMessage);
        }
        catch (IOException ex) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMesServer", ButtonType.OK);
        }
    }
}
