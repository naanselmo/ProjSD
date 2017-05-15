package org.komparator.security.handler;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class SignatureModifierAttackHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "signature";
	private static final String NAME_SENDER = "sender_id";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.SignatureHandler";

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

				// Change the signature slightly
				Node nodeSignature = header.getElementsByTagNameNS(NAMESPACE_URI, NAME).item(0);
				String newSignature = nodeSignature.getFirstChild().getNodeValue().substring(0,4) + '0'
														+ nodeSignature.getFirstChild().getNodeValue().substring(5);
				nodeSignature.getFirstChild().setNodeValue(newSignature);
			} else {
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

}
