package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;

/**
 * Thread accepting new clients connecting to the KMes messenger
 *
 * @version v2.0.2 | last edit: 31.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketWrapper {

    private Socket client;
    private DataInputStream reader;
    private DataOutputStream writer;

    private final EncryptionUtils encryptionUtils;
    private SecretKey AESKey;

    private boolean running;

    public SocketWrapper(Socket pSocket, SecretKey pAESKey) throws IOException
    {
        client = pSocket;
        reader = new DataInputStream(client.getInputStream());
        writer = new DataOutputStream(client.getOutputStream());

        encryptionUtils = new EncryptionUtils();
        AESKey = pAESKey;
    }

    public SocketWrapper(Socket pSocket) throws IOException {
        this(pSocket, EncryptionUtils.generateSymmetricKey());
    }

    protected void close() {
        try
        {
            reader.close();
            writer.close();
            client.close();
        }
        catch (IOException ignored)
        {
            reader = null;
            writer = null;
            client = null;
        }
    }

    protected void writeAES(String pMessage) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        byte[] lEncrpytedMessage = EncryptionUtils.encryptAES(pMessage, AESKey);
        byte[] lMessageSizeAsBytes = ByteBuffer.allocate(4).putInt(lEncrpytedMessage.length).array();
        byte[] lConcatenated = ByteBuffer.allocate(lMessageSizeAsBytes.length+lEncrpytedMessage.length).put(lMessageSizeAsBytes).put(lEncrpytedMessage).array();
        writer.write(lConcatenated, 0, lConcatenated.length);
    }

    protected void writeUnencrypted(String pMessage) throws IOException {
        writer.writeUTF(pMessage);
    }

    protected void writeUnencrypted(byte[] pMessage) throws IOException {
        writer.write(pMessage);
    }

    public String readUTF() throws IOException {
        return reader.readUTF();
    }

    public String readAllBytes() throws IOException {

        return reader.readFully();
    }

    public String readAES() throws IOException
    {
        try
        {
            int lSize = reader.readInt();
            if (lSize <= 0) return "";
            byte[] lEncryptedInput = new byte[lSize];

            reader.readFully(lEncryptedInput);

            return EncryptionUtils.decryptAES(lEncryptedInput, AESKey);
        }
        catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            ex.printStackTrace();
            return "Unable to decrypt message";
        }
    }

}
