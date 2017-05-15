package org.komparator.security.handler;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class SignaturePriceAttackHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "searchProductsResponse";
	private static final String NAMESPACE = "ns2";
	private static final String NAMESPACE_URI = "http://ws.supplier.komparator.org/";

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

				Node searchResultsNode = header.getElementsByTagNameNS(NAMESPACE_URI, NAME).item(0);
				if (searchResultsNode != null) {
					System.out.println(searchResultsNode.getNodeValue());
				}
				if (searchResultsNode != null && searchResultsNode.getFirstChild() != null) {
					System.out.println(searchResultsNode.getFirstChild().getNodeValue());
				}
				if (searchResultsNode != null && searchResultsNode.getFirstChild() != null && searchResultsNode.getFirstChild().getLastChild() != null) {
					System.out.println(searchResultsNode.getFirstChild().getLastChild().getNodeValue());
				}
				if (searchResultsNode != null && searchResultsNode.getFirstChild() != null && searchResultsNode.getFirstChild().getLastChild() != null) {
					searchResultsNode.getFirstChild().getLastChild().setNodeValue(Long.toString(999999));
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

}
