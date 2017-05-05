package org.komparator.security.handler;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Set;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import org.komparator.security.CryptoUtil;
import org.komparator.security.CryptoException;
import org.komparator.security.SecurityConfig;
import java.security.*;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;
import java.security.cert.Certificate;

public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "signature";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.SignatureHandler";
	private static final String SENDER_ID = "sender_id";

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private CAClient certificateAuthority;
	private Map<String, Certificate> localCertificates = new HashMap<>();

	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		try {
			SOAPMessage message = context.getMessage();
			SOAPPart part = message.getSOAPPart();
			Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();

			if (outbound) {
				if (header == null) {
					header = envelope.addHeader();
				}

				message.setProperty(SENDER_ID, SecurityConfig.getProperty(SecurityConfig.PROPERTY_CA_WS_URL));

				if (privateKey == null) {
					try {
						privateKey = CryptoUtil.getKeyFromKeyStore(CryptoUtil.getKeyStoreFromResource(
							SecurityConfig.getProperty(
								SecurityConfig.PROPERTY_KEYSTORE_PATH),
								SecurityConfig.getProperty(SecurityConfig.PROPERTY_KEYSTORE_PASSWORD)
							),
							SecurityConfig.getProperty(SecurityConfig.PROPERTY_KEYSTORE_KEY_ALIAS),
							SecurityConfig.getProperty(SecurityConfig.PROPERTY_KEYSTORE_KEY_PASSWORD)
						);
					} catch (CryptoException e) {
						e.printStackTrace();
						return false;
					}
				}

				// Generate signature
				byte[] byteContent = part.getTextContent().getBytes();
				byte[] byteSignature = CryptoUtil.makeDigitalSignature(privateKey, byteContent);
				String stringSignature = Base64.getEncoder().encodeToString(byteSignature);

				// Insert signature
				Name name = envelope.createName(NAME, NAMESPACE, NAMESPACE_URI);
				SOAPElement element = header.addChildElement(name);
				element.addTextNode(stringSignature);
			} else {
				if (header == null) {
					generateSOAPErrorMessage(message, "No SOAP Header.");
				}

				// Get signature
				Node nodeSignature = header.getElementsByTagNameNS(NAMESPACE_URI, NAME).item(0);
				if (nodeSignature == null || nodeSignature.getFirstChild() == null ) {
					generateSOAPErrorMessage(message, "No properly formatted signature element found in SOAP header.");
				}
				byte[] byteSignature = Base64.getDecoder().decode(nodeSignature.getFirstChild().getNodeValue());

				// Remove signature header
				header.removeChild(nodeSignature);

				if (certificateAuthority == null){
					try {
						certificateAuthority = new CAClient(SecurityConfig.getProperty(SecurityConfig.PROPERTY_CA_WS_URL));
					} catch (CAClientException e) {
						e.printStackTrace();
						return false;
					}
				}

				// Get sender's certificate
				String sender = message.getProperty(SENDER_ID).toString();
				Certificate certificate;
				if (localCertificates.containsKey(sender)) {
					certificate = localCertificates.get(sender);
				} else {
					try {
						certificate = CryptoUtil.getCertificateFromPEMString(certificateAuthority.getCertificate(SENDER_ID));
						if (!CryptoUtil.verifyIssuer(certificate, CryptoUtil.getCertificateFromResource(SecurityConfig.CA_CERTIFICATE_PATH))) {
							generateSOAPErrorMessage(message, "Unsigned certificate received, rejecting!");
						}
					} catch (CryptoException e)	{
						e.printStackTrace();
						return false;
					}
					localCertificates.put(sender, certificate);
				}

				// Verify signature
				byte[] byteContent = part.getTextContent().getBytes();
				if (!CryptoUtil.verifyDigitalSignature(certificate, byteContent, byteSignature)) {
					generateSOAPErrorMessage(message, "Signature mismatch!");
				}
			}
		} catch (SOAPException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext soapMessageContext) {
		return true;
	}

	@Override
	public void close(MessageContext messageContext) {
	}

	private void generateSOAPErrorMessage(SOAPMessage msg, String reason) {
		try {
			SOAPBody soapBody = msg.getSOAPPart().getEnvelope().getBody();
			SOAPFault soapFault = soapBody.addFault();
			soapFault.setFaultString(reason);
			throw new SOAPFaultException(soapFault);
		} catch (SOAPException e) {
			System.err.println("Unable to generate a SOAP error message.");
		}
	}

}
