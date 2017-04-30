package org.komparator.security;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.komparator.security.CryptoUtil.asymCipher;
import static org.komparator.security.CryptoUtil.asymDecipher;

public class CryptoUtilTest {

    // static members
    private static Key publicKey;
    private static Key privateKey;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(CryptoUtilTest.class.getResourceAsStream("/example.jks"), "1nsecure".toCharArray());
        privateKey = ks.getKey("example", "ins3cur3".toCharArray());

        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) f.generateCertificate(CryptoUtilTest.class.getResourceAsStream("/example.cer"));
        publicKey = certificate.getPublicKey();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // runs once after all tests in the suite
    }

    @Test
    public void success() throws CryptoException {
        String message = "test1";
        byte[] content = message.getBytes();
        Assert.assertEquals(message, new String(asymDecipher(asymCipher(content, publicKey), privateKey)));
    }

    @Test(expected = CryptoException.class)
    public void nullKey() throws CryptoException {
        CryptoUtil.asymCipher("data".getBytes(), null);
        CryptoUtil.asymDecipher("data".getBytes(), null);
    }

    @Test(expected = CryptoException.class)
    public void nullData() throws CryptoException {
        CryptoUtil.asymCipher(null, publicKey);
        CryptoUtil.asymDecipher(null, privateKey);
    }

    @Test(expected = CryptoException.class)
    public void alteredCypheredData() throws CryptoException {
        String message = "test1";
        byte[] content = message.getBytes();
        byte[] cyphered = asymCipher(content, publicKey);
        cyphered[2] = (byte) (~cyphered[2] & 0xff);
        asymDecipher(cyphered, privateKey);
    }

}
