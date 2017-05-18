package org.komparator.mediator.ws;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

import javax.xml.ws.WebServiceException;
import java.util.TimerTask;

public class LifeProof extends TimerTask {

	private MediatorClient mediatorClient;
	private MediatorEndpointManager mediatorEndpoint;

	public LifeProof(MediatorEndpointManager mediatorEndpoint) {
		this.mediatorEndpoint = mediatorEndpoint;
	}

	@Override
	public void run() {
		if (MediatorConfig.getBooleanProperty(MediatorConfig.PROPERTY_REDUNDANCY_PRIMARY)) {
			try {
				if (mediatorClient == null) {
					this.mediatorClient = new MediatorClient(MediatorConfig.getProperty(MediatorConfig.PROPERTY_REDUNDANCY_SECONDARY_WS_URL));
				}
				this.mediatorClient.imAlive();
			} catch (WebServiceException | MediatorClientException e) {
				System.err.println("Unable to connect to the backup server.");
				this.mediatorClient = null;
			}
		} else {
			System.out.println("Primary server has not responded in a while. Secondary server will now become primary.");
			try {
				mediatorEndpoint.publishToUDDI();
			} catch (Exception e) {
				System.out.println("Failed to publish to UDDI when attempting to replace primary server.");
				e.printStackTrace();
			}
		}
	}

}
