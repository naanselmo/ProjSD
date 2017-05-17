package org.komparator.mediator.ws;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

import javax.xml.ws.WebServiceException;
import java.util.TimerTask;

public class LifeProof extends TimerTask {

	@Override
	public void run() {
		if (MediatorConfig.getBooleanProperty(MediatorConfig.PROPERTY_REDUNDANCY_PRIMARY)) {
			try {
				MediatorClient client = new MediatorClient(MediatorConfig.getProperty(MediatorConfig.PROPERTY_REDUNDACY_SECONDARY_WS_URL));
//				client.imAlive();
			} catch (WebServiceException | MediatorClientException e) {
				System.err.println("Unable to connect to the backup server.");
			}
		}
	}

}
