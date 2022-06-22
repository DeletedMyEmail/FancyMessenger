package ServerSide;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Hasher class to encode strings to hashes
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class PasswordHasher {

    private final static int cost = 16;

    private final static int key_length = 512;

    private final static String algorithm_name = "PBKDF2WithHmacSHA512";

    //private final SecureRandom random;

    private final byte[] salt = {0,1,0,1,1,1,1,0,1,0,1,1,1,0,1,0};

    public PasswordHasher()
    {
        //random = new SecureRandom();
        //salt = new byte[cost];
        //random.nextBytes(salt);
    }

    protected String getHash(String password) {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, key_length);
        String hash_str;
        try
        {
            SecretKeyFactory key_factory = SecretKeyFactory.getInstance(algorithm_name);
            byte[] hash_value = key_factory.generateSecret(spec).getEncoded();
            Base64.Encoder encoder = Base64.getEncoder();
            hash_str = encoder.encodeToString(hash_value);
        }
        catch (Exception ex)
        {
            hash_str = ex.getMessage();
        }
        return hash_str;
    }

    public static void main(String[] args) {
        PasswordHasher pwhasher = new PasswordHasher();
        System.out.println(pwhasher.getHash("TestPw"));
    }
}
