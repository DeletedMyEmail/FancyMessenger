package de.clientside;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.Socket;

/**
 * Client backend for KMes Messenger
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    private Socket server;
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

    public void addContact(String text) {
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
                                        SceneManager.getHomeScene().showNewMessage(input_str[2], input_str[3]);
                                        break;
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
