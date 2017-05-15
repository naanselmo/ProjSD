package org.komparator.security.handler;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class SignatureTimestampAttackHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME_TIMESTAMP = "timestamp";
	private static final String NAME_NONCE = "nonce";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.TimestampHandler";

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

				// Change the timestamp to see if we could theoretically replay a message
				Node timestampNode = header.getElementsByTagNameNS(NAMESPACE_URI, NAME_TIMESTAMP).item(0);
				timestampNode.getFirstChild().setNodeValue(Long.toString(System.currentTimeMillis() + 1));
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
