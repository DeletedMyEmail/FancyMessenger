package server;

import java.io.*;


/**
 * KMes Server cmd terminal
 *
 * @version v2.0.0 | last edit: 26.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ServerTerminal {

    private BufferedReader reader;
    private SocketManager socketManager;
    private InputHandler inputHandler;

    private boolean active;

    public ServerTerminal() {
        try {
            socketManager = new SocketManager();
            inputHandler = new InputHandler(socketManager);
            inputHandler.start();
        } catch (Exception ex) {
            System.out.println("Error occured while initializing: "+ex);
            System.exit(0);
        }

        reader = new BufferedReader(new InputStreamReader(System.in));
        active = true;
    }

    public void run() {
        printHelp();

        while (active) {
            try {
                String input = reader.readLine().toLowerCase();

                switch (input.strip()) {
                    case "help" -> printHelp();
                    case "esc" -> {
                        active = false;
                        inputHandler.stopListeningForInput();
                        socketManager.close();
                        System.out.println("Server stopped");
                        System.exit(0);
                    }
                    case "connections" -> {
                        int lSize = socketManager.amountOfConnections();
                        System.out.println("Current amount of connection: " + lSize);
                    }
                    case "stopaccepting" -> {
                        socketManager.stopAcceptingSockets();
                        System.out.println("The established connections are still alive but new sockets won't be able to connect anymore");
                    }
                    default -> System.out.println("Command not found\nTry dcs help");
                }
            } catch (IOException e) {
                System.out.println("Error occured:\n" + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                        
                        You can manage the server via the following commands:
                                        
                            - stopaccepting => Keep established connections but accept only one new socket
                            - connections => Return the amount of connected clients
                            - help => Print this list of commands
                            - esc => Exit the application
                                    
                        """);
    }

    public static void main(String[] args) {
        new ServerTerminal().run();
    }
}