package org.komparator.security;


import org.junit.Test;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.*;

public class DigitalSignatureTest {

	final static String CERTIFICATE = "/example.cer";

	final static String KEYSTORE = "/example.jks";
	final static String KEYSTORE_PASSWORD = "1nsecure";

	final static String KEY_ALIAS = "example";
	final static String KEY_PASSWORD = "ins3cur3";

	private final String plainText = "This is the plain text!";
	private final byte[] plainBytes = plainText.getBytes();

	/**
	 * Generate a digital signature using the signature object provided by Java.
	 */
	@Test
	public void testSignature() throws CryptoException {
		// make digital signature
		KeyStore keyStore = CryptoUtil.getKeyStoreFromResource(KEYSTORE, KEYSTORE_PASSWORD);
		PrivateKey privateKey = CryptoUtil.getKeyFromKeyStore(keyStore, KEY_ALIAS, KEY_PASSWORD);
		byte[] digitalSignature = CryptoUtil.makeDigitalSignature(privateKey, plainBytes);
		assertNotNull(digitalSignature);

		// verify the signature
		PublicKey publicKey = CryptoUtil.getCertificateFromResource(CERTIFICATE).getPublicKey();
		boolean result = CryptoUtil.verifyDigitalSignature(publicKey, plainBytes, digitalSignature);
		assertTrue(result);

		// data modification ...
		plainBytes[3] = 12;

		// verify the signature
		boolean resultAfterTamper = CryptoUtil.verifyDigitalSignature(publicKey, plainBytes, digitalSignature);
		assertFalse(resultAfterTamper);
	}

}