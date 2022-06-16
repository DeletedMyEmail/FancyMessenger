package ServerSide;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

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
            System.out.printf("Salt: %s \n",encoder.encodeToString(salt));
        }
        catch (Exception ex)
        {
            hash_str = ex.getMessage();
        }
        return hash_str;
    }

}
