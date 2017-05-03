package org.komparator.security.handler;

import org.w3c.dom.NodeList;

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

public class TimestampHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME_TIMESTAMP = "timestamp";
	private static final String NAME_NONCE = "nonce";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.TimestampHandler";
	private static final int TIMESTAMP_TIMEOUT = 3000;

	private SortedMap<Long, Set<String>> recent_nonces = new TreeMap<Long, Set<String>>();
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
				element.addTextNode(new String(nonce));
			} else {
				if (header == null) {
					generateSOAPErrorMessage(message, "No SOAP Header.");
				}

				// Validate timestamp
				NodeList timestampNodeList = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_TIMESTAMP);
				if (timestampNodeList.getLength() == 0) {
					generateSOAPErrorMessage(message, "No timestamp element found in SOAP header.");
				}
				String timestampString = timestampNodeList.item(0).getChildNodes().item(0).getNodeValue();
				long timestamp = Long.parseLong(timestampString);
				if (Math.abs(System.currentTimeMillis() - timestamp) >= TIMESTAMP_TIMEOUT) {
					generateSOAPErrorMessage(message, "Timestamp discrepancy larger than 3 seconds.");
				}

				// Prune old nonces
				recent_nonces = recent_nonces.tailMap(System.currentTimeMillis() - TIMESTAMP_TIMEOUT);

				// Validate nonce
				NodeList nonceNodeList = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_NONCE);
				if (nonceNodeList.getLength() == 0) {
					generateSOAPErrorMessage(message, "No nonce element found in SOAP header.");
				}
				String nonce = nonceNodeList.item(0).getChildNodes().item(0).getNodeValue();
				if (recent_nonces.containsKey(timestamp)) {
					if (!recent_nonces.get(timestamp).add(nonce)) {
						generateSOAPErrorMessage(message, "Repeated nonce was received.");
					}
				} else {
					Set<String> timestamped_nonces = new HashSet<String>();
					timestamped_nonces.add(nonce);
					recent_nonces.put(timestamp, timestamped_nonces);
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
