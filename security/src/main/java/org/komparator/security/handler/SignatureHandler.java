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
import org.komparator.security.CryptoUtil;
import org.komparator.security.CryptoException;
import org.komparator.security.SecurityConfig;
import java.security.*;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;
import java.security.cert.Certificate;
import java.io.ByteArrayOutputStream;

public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "signature";
	private static final String NAME_SENDER = "sender_id";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.SignatureHandler";

	private final HandlerManager manager = HandlerManager.getInstance();

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

				// Add sender ID
				Name name = envelope.createName(NAME_SENDER, NAMESPACE, NAMESPACE_URI);
				SOAPElement element = header.addChildElement(name);
				element.addTextNode(SecurityConfig.getProperty(SecurityConfig.PROPERTY_CA_ID));

				if (manager.privateKey == null) {
					try {
						manager.privateKey = CryptoUtil.getKeyFromKeyStore(CryptoUtil.getKeyStoreFromResource(
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

				// Get all text
				ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
				message.writeTo(messageStream);
				byte[] byteContent = messageStream.toByteArray();

				// Generate signature
				byte[] byteSignature = CryptoUtil.makeDigitalSignature(manager.privateKey, byteContent);
				String stringSignature = Base64.getEncoder().encodeToString(byteSignature);

				// Insert signature
				name = envelope.createName(NAME, NAMESPACE, NAMESPACE_URI);
				element = header.addChildElement(name);
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

				if (manager.certificateAuthority == null){
					try {
						manager.certificateAuthority = new CAClient(SecurityConfig.getProperty(SecurityConfig.PROPERTY_CA_WS_URL));
					} catch (CAClientException e) {
						e.printStackTrace();
						return false;
					}
				}

				// Get sender's name
				Node nodeSender = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_SENDER).item(0);
				if (nodeSender == null || nodeSender.getFirstChild() == null ) {
					generateSOAPErrorMessage(message, "No properly formatted signature element found in SOAP header.");
				}
				String sender = nodeSender.getFirstChild().getNodeValue();

				// Get sender's certificate
				Certificate certificate;
				if (manager.localCertificates.containsKey(sender)) {
					certificate = manager.localCertificates.get(sender);
				} else {
					try {
						certificate = CryptoUtil.getCertificateFromPEMString(manager.certificateAuthority.getCertificate(sender));
						if (!CryptoUtil.verifyIssuer(certificate, CryptoUtil.getCertificateFromResource(SecurityConfig.CA_CERTIFICATE_PATH))) {
							generateSOAPErrorMessage(message, "Unsigned certificate received, rejecting!");
						}
					} catch (CryptoException e)	{
						e.printStackTrace();
						return false;
					}
					manager.localCertificates.put(sender, certificate);
				}

				// Get all text
				ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
				message.writeTo(messageStream);
				byte[] byteContent = messageStream.toByteArray();

				// Verify signature
				if (!CryptoUtil.verifyDigitalSignature(certificate, byteContent, byteSignature)) {
					generateSOAPErrorMessage(message, "Signature mismatch!");
				}

				// Remove sender's name header
				header.removeChild(nodeSender);
			}
		} catch (SOAPException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
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
