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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.Objects;


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

						int nodeId = 0;
						long timestamp = 0;
						int value = 0;

						boolean success = true;

						if(response.getResponseText() == null || response.getResponseText().isEmpty())
							return;

						try {
							JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(response.getPayload()));

							nodeId = Integer.parseInt(sensorMessage.get("node_id").toString());
							timestamp = Integer.parseInt(sensorMessage.get("timestamp").toString());
							value = Integer.parseInt(sensorMessage.get("value").toString());

						} catch (ParseException pe) {
							System.out.println(response.getResponseText());
							logger.error("Impossible to parse the response!", pe);
							success = false;
						}

						if(ip.endsWith(Integer.toString(nodeId))) {
							if(!DBService.addObservation(ip, value, timestamp)) {
								logger.warn("Impossible to add new observation!");
								success = false;
							}
						} else {
							logger.warn("Message destination is incorrect!");
						}

						if(!success)
							return;

						Request req = new Request(Code.POST);

						if (value < getLowerBound(dataType)) {
							logger.warn(dataType + " too low! (" + value + ")");
							req.setURI("coap://[" + ip + "]/" + dataType + "_switch?color=b");
							req.send();
						} else if (value > getUpperBound(dataType)) {
							logger.warn(dataType + " too high! (" + value + ")");
							req.setURI("coap://[" + ip + "]/"+dataType+"_switch?color=r");
							req.send();
						} else {
							logger.info(dataType + " at normal level. " + value + ")");
							req.setURI("coap://[" + ip + "]/" + dataType + "_switch?color=g");
							req.send();
						}
						DBService.addObservation(ip, value, timestamp);

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
