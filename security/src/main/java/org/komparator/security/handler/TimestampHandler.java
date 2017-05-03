package org.komparator.security.handler;

import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Set;

public class TimestampHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "timestamp";
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
			Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			SOAPHeader header = envelope.getHeader();
			if (outbound) {
				if (header == null) {
					header = envelope.addHeader();
				}

				Name name = envelope.createName(NAME, NAMESPACE, NAMESPACE_URI);
				SOAPElement element = header.addChildElement(name);
				element.addTextNode(Long.toString(System.currentTimeMillis()));
			} else {
				if (header == null) {
					generateSOAPErrorMessage(message, "No SOAP Header.");
				}
				NodeList timestampNodeList = header.getElementsByTagNameNS(NAMESPACE_URI, NAME);
				if (timestampNodeList.getLength() == 0) {
					generateSOAPErrorMessage(message, "No timestamp element found in SOAP header.");
				}
				String timestampString = timestampNodeList.item(0).getChildNodes().item(0).getNodeValue();
				long timestamp = Long.parseLong(timestampString);
				if (Math.abs(System.currentTimeMillis() - timestamp) >= 3000) {
					generateSOAPErrorMessage(message, "Timestamp discrepancy larger than 3 seconds.");
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