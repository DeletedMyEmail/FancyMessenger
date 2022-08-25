package server;

import java.io.*;
import java.sql.SQLException;

public class ServerTerminal {

    private BufferedReader reader;
    private SocketManager socketManager;
    private InputHandler inputHandler;

    private boolean active;
    private String helpText = """
                        
                        You can manage the server via the following commands:
                                        
                            - stopaccepting => Keep established connections but accept only one new socket
                            - help => Print this list of commands
                            - esc => Exit the application
                                    
                        """;

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
        System.out.println("KMes Server online!\n"+helpText);

        while (active) {
            try {
                String input = reader.readLine().toLowerCase();

                switch (input.strip()) {
                    case "help" -> System.out.println(helpText);
                    case "esc" -> {
                        active = false;
                        socketManager.close();
                        inputHandler.stopListeningForInput();
                        System.out.println("Server stopped");
                        System.exit(0);
                    }
                    case "connections" -> {
                        int lSize = socketManager.amountOfConnections();
                        System.out.println("Current amount of connection: " + lSize);
                    }
                    case "disablenewconnections" -> {
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

    public static void main(String[] args) {
        new ServerTerminal().run();
    }
}