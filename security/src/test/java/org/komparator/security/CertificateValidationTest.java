package org.komparator.security;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

public class CertificateValidationTest {

	private static final String CA_CERTIFICATE = "/ca.cer";
	private static final String CERTIFICATE = "/signed_example.cer";
	private static final String NON_SIGNED_CERTIFICATE = "/example.cer";

	@Test
	public void signedCertificateTest() throws CryptoException, CertificateEncodingException, IOException {
		Certificate caCertificate = CryptoUtil.getCertificateFromResource(CA_CERTIFICATE);
		Certificate certificate = CryptoUtil.getCertificateFromResource(CERTIFICATE);
		Assert.assertTrue(CryptoUtil.verifyIssuer(certificate, caCertificate));
	}

	@Test
	public void nonSignedCertificateTest() throws CryptoException {
		Certificate caCertificate = CryptoUtil.getCertificateFromResource(CA_CERTIFICATE);
		Certificate certificate = CryptoUtil.getCertificateFromResource(NON_SIGNED_CERTIFICATE);
		Assert.assertFalse(CryptoUtil.verifyIssuer(certificate, caCertificate));
	}

}