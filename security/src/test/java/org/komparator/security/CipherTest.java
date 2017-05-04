package org.komparator.security;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.komparator.security.CryptoUtil.asymCipher;
import static org.komparator.security.CryptoUtil.asymDecipher;

public class CipherTest {

	// static members
	private static Key publicKey;
	private static Key privateKey;

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws CryptoException {
		KeyStore keyStore = CryptoUtil.getKeyStoreFromResource("/example.jks", "1nsecure");
		privateKey = CryptoUtil.getKeyFromKeyStore(keyStore, "example", "ins3cur3");
		Certificate certificate = CryptoUtil.getCertificateFromResource("/example.cer");
		publicKey = CryptoUtil.getKeyFromCertificate(certificate);
	}

	@AfterClass
	public static void oneTimeTearDown() {
		// runs once after all tests in the suite
	}

	@Test
	public void successCipherTest() throws CryptoException {
		String message = "test1";
		byte[] content = message.getBytes();
		byte[] cypheredData = asymCipher(content, publicKey);
		Assert.assertEquals(message, new String(asymDecipher(cypheredData, privateKey)));
	}

	@Test(expected = CryptoException.class)
	public void nullKeyTest() throws CryptoException {
		CryptoUtil.asymCipher("data".getBytes(), null);
		CryptoUtil.asymDecipher("data".getBytes(), null);
	}

	@Test(expected = CryptoException.class)
	public void nullDataTest() throws CryptoException {
		CryptoUtil.asymCipher(null, publicKey);
		CryptoUtil.asymDecipher(null, privateKey);
	}

	@Test(expected = CryptoException.class)
	public void alteredCipheredDataTest() throws CryptoException {
		String message = "test1";
		byte[] content = message.getBytes();
		byte[] cyphered = asymCipher(content, publicKey);
		cyphered[2] = (byte) (~cyphered[2] & 0xff);
		asymDecipher(cyphered, privateKey);
	}

}