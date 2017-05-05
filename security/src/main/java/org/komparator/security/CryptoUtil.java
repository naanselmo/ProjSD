package org.komparator.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class CryptoUtil {

	private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
	private static final String CERTIFICATE_TYPE = "X.509";
	private static final String KEYSTORE_TYPE = "JKS";
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

	public static KeyStore getKeyStore(InputStream keyStoreInputStream, String storePassword) throws CryptoException {
		try {
			KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
			keyStore.load(keyStoreInputStream, storePassword.toCharArray());
			return keyStore;
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
			throw new CryptoException("Couldn't load key store.", e);
		}
	}

	public static KeyStore getKeyStoreFromResource(String path, String storePassword) throws CryptoException {
		InputStream stream = getResourceAsStream(path);
		KeyStore keyStore = getKeyStore(stream, storePassword);
		closeInputStream(stream);
		return keyStore;
	}

	public static PrivateKey getKeyFromKeyStore(KeyStore keyStore, String keyAlias, String keyPassword) throws CryptoException {
		try {
			return (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
			throw new CryptoException("Couldn't load key from key store.", e);
		}
	}

	public static Certificate getCertificate(InputStream inputStream) throws CryptoException {
		try {
			CertificateFactory factory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
			return factory.generateCertificate(inputStream);
		} catch (CertificateException e) {
			throw new CryptoException("Couldn't load certificate.", e);
		}
	}

	public static Certificate getCertificateFromResource(String path) throws CryptoException {
		return getCertificate(getResourceAsStream(path));
	}

	public static Certificate getCertificateFromPEMString(String certificateString) throws CryptoException {
		InputStream stream = getInputStreamFromString(certificateString);
		Certificate certificate = getCertificate(stream);
		closeInputStream(stream);
		return certificate;
	}

	public static PublicKey getKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}

	public static byte[] asymCipher(byte[] data, Key key) throws CryptoException {
		if (data == null) throw new CryptoException("Data can't be null.");
		if (key == null) throw new CryptoException("Key can't be null.");
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
			throw new CryptoException("Unable to cipher.", e);
		}
	}

	public static byte[] asymDecipher(byte[] data, Key key) throws CryptoException {
		if (data == null) throw new CryptoException("Data can't be null.");
		if (key == null) throw new CryptoException("Key can't be null.");
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
			throw new CryptoException("Unable to decipher.", e);
		}
	}

	// Signatures

	public static byte[] makeDigitalSignature(final PrivateKey privateKey, final byte[] bytesToSign) {
		try {
			Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
			sig.initSign(privateKey);
			sig.update(bytesToSign);
			return sig.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return null;
		}
	}

	public static boolean verifyDigitalSignature(PublicKey publicKey, byte[] bytesToVerify, byte[] signature) {
		try {
			Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
			sig.initVerify(publicKey);
			sig.update(bytesToVerify);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return false;
		}
	}

	public static boolean verifyDigitalSignature(Certificate publicKeyCertificate, byte[] bytesToVerify, byte[] signature) {
		return verifyDigitalSignature(getKeyFromCertificate(publicKeyCertificate), bytesToVerify, signature);
	}

	public static boolean verifyIssuer(Certificate certificate, Certificate issuer) {
		try {
			PublicKey key = getKeyFromCertificate(issuer);
			certificate.verify(key);
			return true;
		} catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
			return false;
		}
	}

	private static InputStream getInputStreamFromString(String string) {
		return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
	}

	/** Method used to access resource. */
	private static InputStream getResourceAsStream(String resourcePath) {
		return CryptoUtil.class.getResourceAsStream(resourcePath);
	}

	private static void closeInputStream(InputStream stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ignored) {
		}
	}

}
