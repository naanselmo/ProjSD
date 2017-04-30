package org.komparator.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    public static byte[] asymCipher(byte[] data, Key key) throws CryptoException {
        if (data == null) throw new CryptoException("Data can't be null.");
        if (key == null) throw new CryptoException("Key can't be null.");
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    public static byte[] asymDecipher(byte[] data, Key key) throws CryptoException {
        if (data == null) throw new CryptoException("Data can't be null.");
        if (key == null) throw new CryptoException("Key can't be null.");
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException(e.getMessage());
        }
    }

}
