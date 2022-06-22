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
    private final String host = "localhost";
    private final int port = 3141;


    public ClientBackend()
    {

    }

    protected static String getUsername() { return username; }

    protected boolean isConnected()
    {
        return client != null && !client.isClosed();
    }

    protected void sendToServer(String message) throws IOException
    {
        output.writeUTF(message);
    }

    private void logIn(String pUsername) {
        username = pUsername;
        SceneController.getSettingsScene().changeUsernameText(username);
        SceneController.switchToSettingsScene();
    }

    private void showNewMessageInGUI()
    {

    }

    public void listenForServerInput () {
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
                                logIn(input_str[2]);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
