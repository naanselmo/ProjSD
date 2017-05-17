package org.komparator.security.handler;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Base64;
import org.komparator.security.CryptoUtil;
import org.komparator.security.CryptoException;
import org.komparator.security.SecurityConfig;
import java.security.*;
import java.security.cert.Certificate;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class CipherHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "cipher";
	private static final String NAME_SECRET_KEY = "secret_key";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.CreditCardCipherHandler";
	private static final String[] MESSAGES_SKIP = new String[] {"imAlive", "updateShopHistory", "updateCart"};
	private static final String OPERATION_TARGET = "T04_Mediator";
	private static final String KEY_ALGO = "AES";

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
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			boolean includeKey = false;

			// Minor hack: disable/skip cipher if, and only if, the message type specifically disables it
			QName operation = (QName) context.get(MessageContext.WSDL_OPERATION);
			for (String skipName : MESSAGES_SKIP) {
				if (operation.getLocalPart().equals(skipName)) {
					return true;
				}
			}

			if (outbound) {
				if (manager.certificateAuthority == null){
					try {
						manager.certificateAuthority = new CAClient(SecurityConfig.getProperty(SecurityConfig.PROPERTY_CA_WS_URL));
					} catch (CAClientException e) {
						e.printStackTrace();
						return false;
					}
				}

				if (manager.publicKey == null && manager.privateKey == null) {
					try {
						Certificate certificate = CryptoUtil.getCertificateFromPEMString(manager.certificateAuthority.getCertificate(OPERATION_TARGET));
						if (!CryptoUtil.verifyIssuer(certificate, CryptoUtil.getCertificateFromResource(SecurityConfig.CA_CERTIFICATE_PATH))) {
							generateSOAPErrorMessage(message, "Unsigned certificate received, rejecting!");
						}
						manager.publicKey = CryptoUtil.getKeyFromCertificate(certificate);
					} catch (CryptoException e) {
						e.printStackTrace();
						return false;
					}
				}

				if (manager.secretKey == null) {
					includeKey = true;
					try {
						SecureRandom randomizer = new SecureRandom();
						KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGO);
						keyGen.init(128, randomizer);
						manager.secretKey = keyGen.generateKey();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						return false;
					}
				}

				List<Node> nodeList = new LinkedList<>();
				nodeList.add(envelope.getBody());
				nodeList.add(envelope.getHeader());
				for (int i = 0; i < nodeList.size(); i++) {
					Node node = nodeList.get(i);
					NodeList children = node.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						nodeList.add(children.item(j));
					}
					if (node.getNodeType() == Node.TEXT_NODE) {
						byte[] nodeContent = node.getNodeValue().getBytes();
						byte[] cipheredNodeContent;
						try {
							cipheredNodeContent = CryptoUtil.symCipher(nodeContent, manager.secretKey);
						} catch (CryptoException e) {
							e.printStackTrace();
							return false;
						}
						node.setNodeValue(Base64.getEncoder().encodeToString(cipheredNodeContent));
					}
				}

				if (includeKey) {
					// Insert symmetric key into header
					if (header == null) {
						header = envelope.addHeader();
					}

					Name name = envelope.createName(NAME_SECRET_KEY, NAMESPACE, NAMESPACE_URI);
					SOAPElement element = header.addChildElement(name);
					byte[] secretKeyBytes = manager.secretKey.getEncoded();
					byte[] cipheredSecretKey;

					try {
						cipheredSecretKey = CryptoUtil.asymCipher(secretKeyBytes, manager.publicKey);
					} catch (CryptoException e) {
						e.printStackTrace();
						return false;
					}

					element.addTextNode(Base64.getEncoder().encodeToString(cipheredSecretKey));
				}
			} else {
				if (manager.publicKey == null && manager.privateKey == null) {
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

				// Fetch secret key
				Node secretKeyNode = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_SECRET_KEY).item(0);
				if (secretKeyNode != null && secretKeyNode.getFirstChild() != null ) {
					byte[] secretKeyCiphered = Base64.getDecoder().decode(secretKeyNode.getFirstChild().getNodeValue());
					byte[] secretKeyUnciphered;
					try {
						secretKeyUnciphered = CryptoUtil.asymDecipher(secretKeyCiphered, manager.privateKey);
					} catch (CryptoException e) {
						e.printStackTrace();
						return false;
					}
					manager.secretKey = new SecretKeySpec(secretKeyUnciphered, 0, secretKeyUnciphered.length, KEY_ALGO);

					// Remove secret key header
					header.removeChild(secretKeyNode);
				} else if (manager.secretKey == null) {
					generateSOAPErrorMessage(message, "No properly formatted secret key found in SOAP header.");
				}

				List<Node> nodeList = new LinkedList<>();
				nodeList.add(envelope.getBody());
				nodeList.add(envelope.getHeader());
				for (int i = 0; i < nodeList.size(); i++) {
					Node node = nodeList.get(i);
					NodeList children = node.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						nodeList.add(children.item(j));
					}
					if (node.getNodeType() == Node.TEXT_NODE) {
						byte[] cipheredNodeContent = Base64.getDecoder().decode(node.getNodeValue());
						byte[] nodeContent;
						try {
							nodeContent = CryptoUtil.symDecipher(cipheredNodeContent, manager.secretKey);
						} catch (CryptoException e) {
							e.printStackTrace();
							return false;
						}
						node.setNodeValue(new String(nodeContent));
					}
				}
			}
		} catch (SOAPException e) {
			throw new RuntimeException(e);
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
