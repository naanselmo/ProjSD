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
import java.security.cert.Certificate;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

public class CreditCardCipherHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String NAME = "cipher";
	private static final String NAMESPACE = "hd1";
	private static final String NAMESPACE_URI = "org.komparator.security.ws.handler.CreditCardCipherHandler";
  private static final String OPERATION_NAME = "buyCart";
  private static final String OPERATION_TARGET = "T04_Mediator";

  private PublicKey publicKey;
  private PrivateKey privateKey;
  private CAClient certificateAuthority;

	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		try {
			SOAPMessage message = context.getMessage();
      SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      Boolean isBuyCart = envelope.getBody() != null &&
                          envelope.getBody().getFirstChild() != null &&
                          envelope.getBody().getFirstChild().getLocalName().equals(OPERATION_NAME);

      if (!isBuyCart) {
        return true;
      }

      if (envelope.getBody().getFirstChild().getLastChild() == null || envelope.getBody().getFirstChild().getLastChild().getFirstChild() == null) {
        generateSOAPErrorMessage(message, "BuyCart operation was improperly formatted, missing CreditCardNr.");
      }

      Node creditCardNode = envelope.getBody().getFirstChild().getLastChild().getFirstChild();

			if (outbound) {
        if (certificateAuthority == null){
					try {
						certificateAuthority = new CAClient(SecurityConfig.getProperty(SecurityConfig.PROPERTY_CA_WS_URL));
					} catch (CAClientException e) {
						e.printStackTrace();
						return false;
					}
				}

        if (publicKey == null) {
          try {
            publicKey = CryptoUtil.getKeyFromCertificate(CryptoUtil.getCertificateFromPEMString(certificateAuthority.getCertificate(OPERATION_TARGET)));
          } catch (CryptoException e) {
            e.printStackTrace();
            return false;
          }
        }

				byte[] byteContent = creditCardNode.getNodeValue().getBytes();
        try {
          CryptoUtil.asymCipher(byteContent, publicKey);
        } catch (CryptoException e) {
          e.printStackTrace();
          return false;
        }
				creditCardNode.setNodeValue(Base64.getEncoder().encodeToString(byteContent));
			} else {
        if (privateKey == null) {
          try {
            privateKey = CryptoUtil.getKeyFromKeyStore(CryptoUtil.getKeyStoreFromResource(
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

				byte[] byteContent = creditCardNode.getNodeValue().getBytes();
        try {
          CryptoUtil.asymDecipher(byteContent, privateKey);
        } catch (CryptoException e) {
          e.printStackTrace();
          return false;
        }
				creditCardNode.setNodeValue(Base64.getEncoder().encodeToString(byteContent));
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
