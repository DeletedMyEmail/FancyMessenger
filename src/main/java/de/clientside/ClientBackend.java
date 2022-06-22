package de.clientside;

import javafx.event.ActionEvent;

import java.io.*;
import java.net.Socket;


/**
 * Client backend for KMes Messenger
 *
 * @version 18.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend extends Thread {

    private static Socket client;
    private static DataInputStream input;
    private static DataOutputStream output;

    private static String username;
    private final String host = "localhost";

    private GUIController controller;

    public ClientBackend()
    {
        ModelClass.addCommand("1");
    }

    protected static String getUsername() { return username; }


    protected boolean isConnected()
    {
        return client != null && !client.isClosed();
    }

    protected static void sendToServer(String message) throws IOException
    {
        output.writeUTF(message);
    }

    private void logIn(String pUsername) throws IOException {
        username = pUsername;
        GUIController.setLoginState(true);
    }

    public void run() {
        try {
            System.out.println("Connecting to server...");
            client = new Socket(host, 3141);
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
}
