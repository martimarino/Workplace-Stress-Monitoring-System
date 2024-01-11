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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import it.unipi.adii.iot.wsms.services.resources.ResRegistration;


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
		this.resSensor = new CoapClient("coap://[" + ipAddress + "]/sensor");
		this.resSwitch = new CoapClient("coap://[" + ipAddress + "]/switch");
		
		CoapObserveRelation observeProperty = this.resSensor.observe(
				new CoapHandler() {
					public void onLoad(CoapResponse response) {

						long timestamp;
						int value;
						int nodeId;

						if(response.getResponseText() == null || response.getResponseText().isEmpty())
							return;
						
						try {
							JSONObject sensorMsg = (JSONObject) JSONValue.parseWithException(response.getResponseText());
							
							timestamp = Integer.parseInt(sensorMsg.get("timestamp").toString());
							nodeId = Integer.parseInt(sensorMsg.get("node_id").toString());
							value = Integer.parseInt(sensorMsg.get("value").toString());

						} catch (ParseException pe) {
							System.out.println(response.getResponseText());
							logger.error("Exception in parsing coap response!", pe);
							return;
						}
						
						if(ip.endsWith(Integer.toString(nodeId))) {
							if(!db_Service.addObservation(ip, value, timestamp)) {
								logger.warn("New observation failed!");
								return;
							}
						} else {
							logger.warn("Destination msg is not correct!");
						}

						// compare values with bounds set
						if (value < getLowerBound(dataType)) {
//							state = 1;
							//String payload = "mode=on";
							Request req = new Request(Code.POST);
//							//req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch?color=b");
							req.send();
							logger.info("[CRITICAL] - " + ip + " - the " + dataType + " value ("+value+") is too low!");
							db_Service.updateSensorState(ip, state);

						} else if(value > getUpperBound(dataType))
						{
//							state = 2;
//							String payload = "mode=on";
							Request req = new Request(Code.POST);
//							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch?color=r");
							req.send();
							logger.info("[CRITICAL] - " + ip + " - the " + dataType + " value ("+value+") is too high!");
							db_Service.updateSensorState(ip, state);
						} else {
//							state = 0;
//							String payload = "mode=on";
							Request req = new Request(Code.POST);
//							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch?color=g");
							req.send();
							logger.info("[NORMAL] - " + ip + " - the " + dataType + " value ("+value+") is comfortable!");
							db_Service.updateSensorState(ip, state);
						}

					}
					
					public void onError() {
						stopObserve = true;
//						logger.error("OBSERVING FAILED with " + dataType + " sensor " + ip);
						
						if(ResRegistration.removeDevice(ip)) {
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
