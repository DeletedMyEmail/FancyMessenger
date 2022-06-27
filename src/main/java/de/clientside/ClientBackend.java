package de.clientside;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Client backend for KMes Messenger
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    // Hostname/IP from KMes Server
    private final String host = "localhost";
    // Port from KMes Server which accepts clients
    private final int port = 3141;

    private Socket server;
    private DataInputStream input;
    private DataOutputStream output;
    private BufferedReader reader;
    private BufferedWriter writer;

    private List<String> contacts;
    private HashMap<String, List<String>> messages;

    private static String username = "";

    public ClientBackend() throws IOException {
        messages = new HashMap<>();

        reader = new BufferedReader(new FileReader("src/main/contacts.txt"));
        writer = new BufferedWriter(new FileWriter("src/main/contacts.txt"));

        contacts = new ArrayList<>();
        String in;
        while((in = reader.readLine()) != null)
        {
            contacts.add(in);
        }
    }

    protected List<String> getMessagesForUser(String user)
    {
        return messages.get(user);
    }

    /**
     * @return Returns the username from current user or an empty string if not logged in.
     * */
    protected String getUsername() { return username; }

    /**
     *
     * */
    protected void logOut() {
        username = "";
        try
        {
            sendToServer("KMES;logout");
        }
        catch (IOException ex)
        {
            try
            {
                server.close();
            }
            catch (IOException e)
            {
                server = null;
            }
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
     * @param pMessage Message to send to server
     * */
    protected void sendToServer(String pMessage) throws IOException
    {
        output.writeUTF(pMessage);
    }

    /**
     * Updates the current user, the GUI and loads the account settings scene
     *
     * @param pUsername Username of the new logged in user
     * */
    private void updateCurrentUser(String pUsername) {
        username = pUsername;
        SceneManager.getSettingsScene().changeUsernameText(username);
        SceneManager.switchToSettingsScene();
    }

    protected void addNewMessage(String pUsername, String pMessage)
    {
        if (messages.get(pUsername) != null)
        {
            messages.get(pUsername).add(pMessage);
        }
        else
        {
            messages.put(pUsername, new ArrayList<String>(){{add(pMessage);}});
        }
        SceneManager.getHomeScene().showNewMessage(pUsername, pMessage);
    }

    /**
     * Creates a new Thread which listens to server inputs and handles them
     * */
    protected void listenForServerInput () {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    while (true)
                    {
                        while (!isConnected())
                        {
                            System.out.println("Connecting to server...");
                            server = new Socket(host, port);
                            output = new DataOutputStream(server.getOutputStream());
                            input = new DataInputStream(server.getInputStream());
                            System.out.println("Successfully connected");
                        }
                        while (isConnected())
                        {
                            String[] input_str = input.readUTF().split(";");
                            if (!input_str[0].equals("KMES"))
                            {
                                server.close();
                            }
                            else
                            {
                                switch (input_str[1]) {
                                    case "loggedIn":
                                        updateCurrentUser(input_str[2]);
                                        break;
                                    case "error":
                                        SceneManager.showError(Alert.AlertType.ERROR, input_str[2], input_str[3], ButtonType.OK);
                                        break;
                                    case "message":
                                        addNewMessage(input_str[2], "Received: "+input_str[3]);
                                        break;
                                    case "userExists":
                                        SceneManager.getHomeScene().showNewContact(input_str[2]);
                                        SceneManager.showError(Alert.AlertType.CONFIRMATION, "Successfully added new contact: "+input_str[2], "New contact", ButtonType.OK);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
