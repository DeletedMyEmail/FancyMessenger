package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;


/**
 * Thread accepting new clients connecting to the KMes Server
 *
 * @version stabel-1.0.5 | last edit: 21.10.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketAcceptor extends Thread {

    private final HashMap<String, InputHandler> clients;
    private final HashMap<String, List<String>> queuedMessages;
    private final ServerSocket serverSocket;

    private final EncryptionUtils encryptionUtils;

    private final SQLUtils sqlUtils;
    private boolean running;

    protected SocketAcceptor(int pPort) throws IOException, SQLException {
        clients = new HashMap<>();
        queuedMessages = new HashMap<>();
        serverSocket = new ServerSocket(pPort);
        encryptionUtils = new EncryptionUtils();
        sqlUtils = new SQLUtils("src/main/resources/server/kmes_server.db");
        sqlUtils.onExecute("""
                CREATE TABLE IF NOT EXISTS User
                (
                    username TEXT primary key,
                    hashedPassword TEXT,
                    salt BLOB
                );
                """);
        sqlUtils.onExecute("""
                CREATE TABLE IF NOT EXISTS Session (
                	ip BLOB,
                	username TEXT,
                	FOREIGN KEY("username") REFERENCES "User"("username"),
                	PRIMARY KEY("ip")
                );
                """);
    }

    /**
     * Accepts new sockets and establishes encryption via RSA + AES key transfer
     * */
    @Override
    public void run()
    {
        running = true;
        System.out.println("Server is now listening for clients on port "+serverSocket.getLocalPort());
        while (running)
        {
            try
            {
                // Init new socket and streams
                SocketWrapper lNewSocket = new SocketWrapper(serverSocket.accept());
                if (!running) {
                    lNewSocket.close();
                    break;
                }

                new InputHandler(lNewSocket, clients, queuedMessages, sqlUtils, encryptionUtils).start();
                System.out.println("Client socket accepted");

            }
            catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void stopAcceptingSockets() { running = false; }

    public static void main(String[] args) throws SQLException, IOException {
        int lPort = 4242;
        if (args.length  > 0)
        {
            try {
                lPort = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException ex) {
                System.out.println("Invalid port");
                System.exit(0);
            }
        }
        new SocketAcceptor(lPort).start();
    }
}
