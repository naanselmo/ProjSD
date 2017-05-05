package org.komparator.security.handler;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.security.SecureRandom;
import java.util.Base64;

public class TimestampHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME_TIMESTAMP = "timestamp";
	private static final String NAME_NONCE = "nonce";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.TimestampHandler";
	private static final int TIMESTAMP_TIMEOUT = 3000;

	private SortedMap<Long, Set<String>> recent_nonces = new TreeMap<>();
	private SecureRandom randomizer = new SecureRandom();

	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		try {
			SOAPMessage message = context.getMessage();
			Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			SOAPHeader header = envelope.getHeader();
			if (outbound) {
				if (header == null) {
					header = envelope.addHeader();
				}

				// Insert timestamp
				Name name = envelope.createName(NAME_TIMESTAMP, NAMESPACE, NAMESPACE_URI);
				SOAPElement element = header.addChildElement(name);
				element.addTextNode(Long.toString(System.currentTimeMillis()));

				// Insert nonce
				name = envelope.createName(NAME_NONCE, NAMESPACE, NAMESPACE_URI);
				element = header.addChildElement(name);
				byte[] nonce = new byte[16];
				randomizer.nextBytes(nonce);
				element.addTextNode(Base64.getEncoder().encodeToString(nonce));
			} else {
				if (header == null) {
					generateSOAPErrorMessage(message, "No SOAP Header.");
				}

				// Validate timestamp
				Node timestampNode = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_TIMESTAMP).item(0);
				if (timestampNode == null || timestampNode.getFirstChild() == null ) {
					generateSOAPErrorMessage(message, "No properly formatted timestamp element found in SOAP header.");
				}
				long timestamp = Long.parseLong(timestampNode.getFirstChild().getNodeValue());
				if (Math.abs(System.currentTimeMillis() - timestamp) >= TIMESTAMP_TIMEOUT) {
					generateSOAPErrorMessage(message, "Timestamp discrepancy larger than 3 seconds.");
				}

				// Remove timestamp header
				header.removeChild(timestampNode);

				// Prune old nonces
				recent_nonces = recent_nonces.tailMap(System.currentTimeMillis() - TIMESTAMP_TIMEOUT);

				// Validate nonce
				Node nonceNode = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_NONCE).item(0);
				if (nonceNode == null || nonceNode.getFirstChild() == null ) {
					generateSOAPErrorMessage(message, "No properly formatted nonce element found in SOAP header.");
				}
				String nonce = nonceNode.getFirstChild().getNodeValue();
				if (recent_nonces.containsKey(timestamp)) {
					if (!recent_nonces.get(timestamp).add(nonce)) {
						generateSOAPErrorMessage(message, "Repeated nonce was received.");
					}
				} else {
					Set<String> timestamped_nonces = new HashSet<>();
					timestamped_nonces.add(nonce);
					recent_nonces.put(timestamp, timestamped_nonces);
				}

				// Remove nonce header
				header.removeChild(nonceNode);
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
