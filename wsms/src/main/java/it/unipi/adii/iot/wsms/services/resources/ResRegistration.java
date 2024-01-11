package it.unipi.adii.iot.wsms.services.resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import it.unipi.adii.iot.wsms.services.DBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.unipi.adii.iot.wsms.device.IoTDevice;

public class ResRegistration extends CoapResource{
	private static final Logger logger = LogManager.getLogger(IoTDevice.class);
	private static final DBService th = DBService.getInstance();
	private static Collection<IoTDevice> ioTDevices = Collections.synchronizedList(new ArrayList<>());
	
	public ResRegistration() {
		super("registration");
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		exchange.accept();
		String dataType = exchange.getRequestText();
        String ipAddress = exchange.getSourceAddress().getHostAddress();
        
		if (contains(ipAddress)<0) {
			if(th.addSensor(ipAddress, dataType)) {
        		synchronized(ioTDevices) {
        			ResRegistration.ioTDevices.add(new IoTDevice(ipAddress, dataType));
        		}
				logger.info("The smart device [" + ipAddress + "] has been registered!");
				exchange.respond(CoAP.ResponseCode.CREATED, "Registration successful!".getBytes(StandardCharsets.UTF_8));
			}
			else {
				logger.error("Impossible to add the device!");
				exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE, "Registration unsuccessful".getBytes(StandardCharsets.UTF_8));
			}
		} else
			logger.warn("Device " + ipAddress + " already registered!");		
	}
	
	@Override
	public void handleDELETE(CoapExchange exchange) {
		String[] request = exchange.getRequestText().split("-");
		String ipAddress = request[0];
		String dataType = request[1];
		boolean success = true;
		
		if (success)
			exchange.respond(CoAP.ResponseCode.DELETED, "Deletion completed!".getBytes(StandardCharsets.UTF_8));
		else
			exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Deletion not completed!".getBytes(StandardCharsets.UTF_8));
	}
	
	private static int contains(final String ipAddress) {
		int idx = -1;
		
		for(IoTDevice device : ioTDevices) {
			idx++;
			if(device.getIP().contentEquals(ipAddress))
				return idx;
		}
		return -1;
	}
	
	public static boolean removeDevice(final String ipAddress) {
		boolean success = true;
		int idx = contains(ipAddress);
		if (idx > -1) {
			synchronized(ioTDevices) {
        		ResRegistration.ioTDevices.remove(idx);
        	}
			
		} else {
			success = false;
		}
		
		return success;
	}
}
