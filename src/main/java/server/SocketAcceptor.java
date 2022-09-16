package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Thread accepting new clients connecting to the KMes messenger
 *
 * @version v2.0.2 | last edit: 31.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketAcceptor extends Thread {

    private static final int PORT = 4242;

    private final HashMap<String, SocketWrapper> clients;
    private final HashMap<String, List<String>> queuedMessages;
    private final ServerSocket serverSocket;

    private final EncryptionUtils encryptionUtils;

    private boolean running;

    protected SocketAcceptor() throws IOException
    {
        clients = new HashMap<>();
        queuedMessages = new HashMap<>();
        serverSocket = new ServerSocket(PORT);
        encryptionUtils = new EncryptionUtils();
    }

    protected void closeSocket(String pUsername) {
        if (clients.get(pUsername) != null) clients.get(pUsername).close();
        clients.remove(pUsername);
    }

    /**
     * Accepts new sockets and establishes secure RSA encryption via key transfer
     * */
    public void run()
    {
        running = true;
        while (running)
        {
            try
            {
                // Init new socket and streams
                SocketWrapper lNewSocket = new SocketWrapper(serverSocket.accept());

                byte[] lInput = new byte[294];
                lNewSocket.getInStream().read(lInput);

                // Key handshake

                PublicKey lForeignPubKey = EncryptionUtils.decodeRSAKey(lInput);
                if (lForeignPubKey == null)  {
                    lNewSocket.close();
                    continue;
                }

                byte[] lEncodedOwnKey = encryptionUtils.getPublicKey().getEncoded();
                lNewSocket.writeUnencrypted(lEncodedOwnKey);

                lInput = new byte[128];
                lNewSocket.getInStream().read(lInput);
                SecretKey lSocketsAESKey = EncryptionUtils.decodeAESKey(
                        encryptionUtils.decryptRSAToBytes(lInput));

                lNewSocket.setAESKey(lSocketsAESKey);

                lNewSocket.getSocket().setSoTimeout(0);

                try
                {
                    new InputHandler(lNewSocket, clients, queuedMessages).start();
                    System.out.println("Client socket accepted");
                }
                catch (Exception ex) {ex.printStackTrace();}

            }
            catch (IOException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
    }

    protected void stopAcceptingSockets() { running = false; }

    protected void close()
    {
        running = false;
        clients.forEach((username, socketWrapper) -> socketWrapper.close());
        clients.clear();
    }

    protected int amountOfConnections() { return clients.size(); }
}
