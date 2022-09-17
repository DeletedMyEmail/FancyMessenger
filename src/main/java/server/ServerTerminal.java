package server;

import java.io.*;


/**
 * KMes Server cmd terminal
 *
 * @version v3.0.0 | last edit: 16.09.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ServerTerminal {

    private int port;

    private BufferedReader reader;
    private SocketAcceptor socketAcceptor;

    public ServerTerminal() {
        reader = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            port = -1;
            while (port < 0)
            {
                try
                {
                    System.out.println("Specify on which port the server runs: ");
                    port = Integer.parseInt(reader.readLine());
                }
                catch (NumberFormatException ignored) {}
            }
            socketAcceptor = new SocketAcceptor(port);
            socketAcceptor.start();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while initializing server");
            System.exit(1);
        }
    }

    public void run() {
        printHelp();

        boolean running = true;
        while (running) {
            try {
                String input = reader.readLine().toLowerCase();

                switch (input.strip()) {
                    case "help" -> printHelp();
                    case "esc" -> {
                        running = false;
                        socketAcceptor.close();
                        System.out.println("Server stopped");
                        System.exit(0);
                    }
                    case "connections" -> {
                        int lSize = socketAcceptor.amountOfConnections();
                        System.out.println("Current amount of connection: " + lSize);
                    }
                    case "stopaccepting" -> {
                        socketAcceptor.stopAcceptingSockets();
                        System.out.println("The established connections are still alive but new sockets won't be able to connect anymore");
                    }
                    case "getport" -> System.out.println(port);
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
                                        
                            - stopaccepting => Keep established connections but don't accept new sockets
                            - connections => Return the amount of connected clients
                            - getport => Returns the port on which the server runs
                            - help => Print this list of commands
                            - esc => Exit the application
                                    
                        """);
    }

    public static void main(String[] args) {
        new ServerTerminal().run();
    }
}