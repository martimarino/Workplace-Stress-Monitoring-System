package it.unipi.adii.iot.wsms.device;

import it.unipi.adii.iot.wsms.services.DBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import it.unipi.adii.iot.wsms.services.resources.ResourceRegistration;


public class IoTDevice {
	private static final int LOWER_BOUND_TEMP = 19; // in °C
	private static final int UPPER_BOUND_TEMP = 27; // in °C
	private static final int LOWER_BOUND_HUM = 30; // in %
	private static final int UPPER_BOUND_HUM = 60; // in %

	private static final int LOWER_BOUND_NOISE = 10; // in dB
	private static final int UPPER_BOUND_NOISE = 60; // in dB

	private static final Logger logger = LogManager.getLogger(IoTDevice.class);
	private static final DBService db_Service = DBService.getInstance();

	private final String ip;
	private CoapClient resSensor;
	private CoapClient resSwitch;
	private boolean stopObserve = false;
	private short state = 0;


	public static int getLowerBound(String dataType) {
		switch (dataType) {
			case "temperature":
				return LOWER_BOUND_TEMP;
			case "humidity":
				return LOWER_BOUND_HUM;
			case "noise":
				return LOWER_BOUND_NOISE;
			default:
				return -1;
		}
	}

	public static int getUpperBound(String dataType) {
		switch (dataType) {
			case "temperature":
				return UPPER_BOUND_TEMP;
			case "humidity":
				return UPPER_BOUND_HUM;
			case "noise":
				return UPPER_BOUND_NOISE;
			default:
				return -1;
		}
	}

		
	public IoTDevice(String ipAddress, String dataType) {

		this.ip = ipAddress;
		this.resSensor = new CoapClient("coap://[" + ipAddress + "]/"+dataType+"_sensor");
		this.resSwitch = new CoapClient("coap://[" + ipAddress + "]/"+dataType+"_switch");
		
		CoapObserveRelation observeProperty = this.resSensor.observe(
				new CoapHandler() {
					public void onLoad(CoapResponse response) {

						if(response.getResponseText() == null || response.getResponseText().isEmpty())
							return;

						String responseTxt = response.getResponseText();
						System.out.println("Ho ricevuto: " + responseTxt);

						Request req = new Request(Code.POST);
						String[] tokens = responseTxt.split(" ");
						int value;
						String warnType;
						int timestamp;

						if(responseTxt.startsWith("WARN")) {
							warnType = tokens[1];
							value = Integer.parseInt(tokens[2]);
							timestamp = Integer.parseInt(tokens[3]);
							switch (warnType) {
								case "low":
									logger.warn(dataType + " too low! (" + tokens[2] + ")");
									req.setURI("coap://[" + ip + "]/"+dataType+"_switch?color=b");
									req.send();
									break;
								case "high":
									logger.warn(dataType + " too high! (" + tokens[2] + ")");
									req.setURI("coap://[" + ip + "]/"+dataType+"_switch?color=r");
									req.send();
									break;
							}
						} else {
							value = Integer.parseInt(tokens[0]);
							timestamp = Integer.parseInt(tokens[1]);
							logger.info(dataType + " at normal level. ");
							req.setURI("coap://[" + ip + "]/"+dataType+"_switch?color=g");
							req.send();
						}
						DBService.addObservation(ip, value, );
					}
					
					public void onError() {
						stopObserve = true;
//						logger.error("OBSERVING FAILED with " + dataType + " sensor " + ip);
						
						if(ResourceRegistration.removeDevice(ip)) {
							db_Service.deleteSensor(ip, dataType);
							logger.error("OBSERVING FAILED with " + dataType + " sensor " + ip);
						} else {
							logger.error("The sensor " + ip + " is not registered");
						}
                    
					}
				}, MediaTypeRegistry.APPLICATION_JSON);
		
		if (stopObserve) {
			observeProperty.proactiveCancel();
		}
	}

	public String getIP() {
		return ip;
	}

}
