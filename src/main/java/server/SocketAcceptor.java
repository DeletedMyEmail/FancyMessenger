package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;


/**
 * Thread accepting new clients connecting to the KMes messenger
 *
 * @version v2.0.2 | last edit: 31.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketAcceptor extends Thread{

    private static final int PORT = 4242;

    private final List<SocketWrapper> clients;
    private final EncryptionUtils encryptionUtils;
    private final ServerSocket serverSocket;

    private boolean running;

    protected SocketAcceptor() throws IOException
    {
        clients = new ArrayList<>();
        serverSocket = new ServerSocket(PORT);
        encryptionUtils = new EncryptionUtils();
    }

    protected void closeSocket(int pIndex) throws IOException {
        clients.get(pIndex).close();
        clients.remove(pIndex);
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
                lNewInStream.read(lInput);

                // Key handshake

                PublicKey lForeignPubKey = EncryptionUtils.decodeRSAKey(lInput);

                if (lForeignPubKey == null)  {
                    lNewSocket.close();
                    lNewInStream.close();
                    lNewOutStream.close();
                    continue;
                }

                byte[] lEncodedOwnKey = encryptionUtils.getPublicKey().getEncoded();
                lNewOutStream.write(lEncodedOwnKey);

                lInput = new byte[128];
                lNewInStream.read(lInput);
                SecretKey lSocketsAESKey = EncryptionUtils.decodeAESKey(
                        encryptionUtils.decryptRSAToBytes(lInput));

                socketConnectionsAndStreams.add(new ArrayList<>()
                {{
                    add(lNewSocket);
                    add(lNewOutStream);
                    add(lNewInStream);
                    add("");
                    add(lSocketsAESKey);
                }});

                System.out.printf("[%d]Client socket accepted\n", socketConnectionsAndStreams.toArray().length);
                lNewSocket.setSoTimeout(100);
            }
            catch (IOException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
    }
    protected void stopAcceptingSockets() { running = false; }

    protected int amountOfConnections() { return socketConnectionsAndStreams.size(); }

    protected void close()
    {
        running = false;
        for (List<Object> connection : socketConnectionsAndStreams)
        {
            try
            {
                ((Socket) connection.get(0)).close();
                ((DataOutputStream) connection.get(1)).close();
                ((DataInputStream) connection.get(2)).close();
            }
            catch (IOException e) {e.printStackTrace();}
            finally {
                socketConnectionsAndStreams.remove(connection);
            }
        }
    }
}
