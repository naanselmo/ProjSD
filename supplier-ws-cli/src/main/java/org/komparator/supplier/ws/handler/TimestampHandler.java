package org.komparator.supplier.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class TimestampHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String NAME = "timestamp";
    private static final String NAMESPACE = "hd1";
    private static final String NAMESPACE_URI = "org.komparator.supplier.ws.handler.TimestampHandler";

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessage message = context.getMessage();
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            try {
                SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.getHeader();
                if (header == null) {
                    header = envelope.addHeader();
                }

                Name name = envelope.createName(NAME, NAMESPACE, NAMESPACE_URI);
                SOAPElement element = header.addChildElement(name);
                element.addTextNode(Long.toString(System.currentTimeMillis()));
            } catch (SOAPException e) {
                e.printStackTrace();
                return false;
            }
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