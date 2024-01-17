package it.unipi.adii.iot.wsms.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapServer;

import it.unipi.adii.iot.wsms.services.resources.ResourceRegistration;

public class RegistrationService extends CoapServer {
	private static final Logger logger = LogManager.getLogger(RegistrationService.class);
	
	public RegistrationService() {
		this.add(new ResourceRegistration());
		logger.info("Coap server started");
    }

}
