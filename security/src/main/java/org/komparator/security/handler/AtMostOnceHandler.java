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

public class AtMostOnceHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME_IDENTIFIER = "identifier";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.AtMostOnceHandler";
	private static final String[] MESSAGES_REUSE = new String[] {"imAlive", "updateShopHistory", "updateCart"};

	private final HandlerManager manager = HandlerManager.getInstance();

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

				if (manager.identifier == null) {
					byte[] identifier = new byte[16];
					manager.randomizer.nextBytes(identifier);
					manager.identifier = Base64.getEncoder().encodeToString(identifier);
				}

				if (manager.lastIdentifier == null) {
					manager.lastIdentifier = "";
				}

				// Minor hack: if message is of a specific type, send it with the last identifier received
				// This ensures that the secondary mediator is also aware of what the last identifier is
				QName operation = (QName) context.get(MessageContext.WSDL_OPERATION);
				boolean reuse = false;
				for (String reuseName : MESSAGES_REUSE) {
					if (operation.getLocalPart().equals(reuseName)) {
						reuse = true;
					}
				}

				// Prepare header for identifier
				Name name = envelope.createName(NAME_IDENTIFIER, NAMESPACE, NAMESPACE_URI);
				SOAPElement element = header.addChildElement(name);

				// Insert the identifier
				if (reuse) {
					element.addTextNode(manager.lastIdentifier);
				} else {
					element.addTextNode(manager.identifier);
				}
			} else {
				if (header == null) {
					generateSOAPErrorMessage(message, "No SOAP Header.");
				}

				// Minor hack: since it's received with the "last" identifier if it's of a specific type
				// we need to NOT dump the message, or we end up losing whatever the contents were
				QName operation = (QName) context.get(MessageContext.WSDL_OPERATION);
				boolean throwable = true;
				for (String reuseName : MESSAGES_REUSE) {
					if (operation.getLocalPart().equals(reuseName)) {
						throwable = false;
					}
				}

				// Validate identifier
				Node identifierNode = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_IDENTIFIER).item(0);
				if (identifierNode == null || identifierNode.getFirstChild() == null ) {
					generateSOAPErrorMessage(message, "No properly formatted identifier element found in SOAP header.");
				}
				String newIdentifier = identifierNode.getFirstChild().getNodeValue();
				if (throwable && newIdentifier.equals(manager.lastIdentifier)) {
					generateSOAPErrorMessage(message, "Repeated identifier was received.");
				}

				// Update last identifier
				manager.lastIdentifier = newIdentifier;

				// Generate new identifier
				byte[] identifier = new byte[16];
				manager.randomizer.nextBytes(identifier);
				manager.identifier = Base64.getEncoder().encodeToString(identifier);

				// Remove identifier header
				header.removeChild(identifierNode);
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
