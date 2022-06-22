package de.clientside;

import java.io.*;
import java.net.Socket;

/**
 * Client backend for KMes Messenger
 *
 * @version 18.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    private Socket client;
    private DataInputStream input;
    private DataOutputStream output;

    private static String username = "";

    // Hostname/IP from KMes Server
    private final String host = "localhost";
    // Port from KMes Server which accepts clients
    private final int port = 3141;


    /**
     * @return Returns the username from current user or an empty string if not logged in.
     * */
    protected static String getUsername() { return username; }

    /**
     * @return Returns the connection status to the KMes Server
     * */
    protected boolean isConnected()
    {
        return client != null && !client.isClosed();
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
        SceneController.getSettingsScene().changeUsernameText(username);
        SceneController.switchToSettingsScene();
    }

    /**
     * Updates the GUI to display new income messages
     * */
    protected void showNewMessageInGUI()
    {

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
                    System.out.println("Connecting to server...");
                    client = new Socket(host, port);
                    output = new DataOutputStream(client.getOutputStream());
                    input = new DataInputStream(client.getInputStream());
                    System.out.println("Successfully connected");

                    while (isConnected()) {
                        String[] input_str = input.readUTF().split(";");
                        if (!input_str[0].equals("KMES")) continue;

                        switch (input_str[1])
                        {
                            case "loggedIn":
                                updateCurrentUser(input_str[2]);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
